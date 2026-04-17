package com.ecommerce.chatdemo.domain.order.service;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cart.exception.CartErrorCode;
import com.ecommerce.chatdemo.domain.cart.exception.CartException;
import com.ecommerce.chatdemo.domain.cart.repository.CartRepository;
import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import com.ecommerce.chatdemo.domain.cartitem.repository.CartItemRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.membercoupon.exception.MemberCouponErrorCode;
import com.ecommerce.chatdemo.domain.membercoupon.exception.MemberCouponException;
import com.ecommerce.chatdemo.domain.membercoupon.repository.MemberCouponRepository;
import com.ecommerce.chatdemo.domain.order.dto.request.CancelOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.request.CreateOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.request.DirectOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.response.CancelOrderResponse;
import com.ecommerce.chatdemo.domain.order.dto.response.OrderResponse;
import com.ecommerce.chatdemo.domain.order.dto.response.GetOrderResponse;
import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.order.entity.OrderStatus;
import com.ecommerce.chatdemo.domain.order.exception.OrderErrorCode;
import com.ecommerce.chatdemo.domain.order.exception.OrderException;
import com.ecommerce.chatdemo.domain.order.repository.OrderRepository;
import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import com.ecommerce.chatdemo.domain.orderitem.entity.repository.OrderItemRepository;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.exception.ProductErrorCode;
import com.ecommerce.chatdemo.domain.product.exception.ProductException;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.AuthException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;

    // 즉시 결제(장바구니X)
    @Transactional
    public OrderResponse createDirectOrder(Long memberId, DirectOrderRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 요청 상품이 존재하는 지 확인
        // 비관적 락 도입
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND)
        );
        // 요청 수량을 받을 수 있는 지 재고 체크
        product.validateStock(request.getQuantity());

        // 총 주문 금액
        long totalAmount = product.getPrice() * request.getQuantity();

        // 쿠폰 유효성 검증
        MemberCoupon memberCoupon = null;
        if (request.getMemberCouponId() != null) {
            memberCoupon = memberCouponRepository.findById(request.getMemberCouponId()).orElseThrow(
                    () -> new MemberCouponException(MemberCouponErrorCode.COUPON_NOT_FOUND)
            );
            memberCoupon.validateCoupon(memberId);
        }
        // 쿠폰 할인 금액
        Long useCouponPrice = memberCoupon != null ? memberCoupon.getCoupon().getDiscountAmount() : 0L;

        // 포인트 검증
        Long usePoints = request.getUsePoints() != null ? request.getUsePoints() : 0L;
        member.validatePoint(usePoints);

        // 주문금액 초과 체크
        if (usePoints > totalAmount) {
            throw new OrderException(OrderErrorCode.EXCEED_POINT_USAGE);
        }

        // 주문 생성(=결제완료)
        String orderNumber = Order.generateOrderNumber();

        Order order = orderRepository.save(Order.create(
                member,
                orderNumber,
                totalAmount,
                usePoints,
                useCouponPrice
        ));
        OrderItem orderItem = orderItemRepository.save(OrderItem.create(
                order,
                product,
                request.getQuantity()
        ));

        // 재고 차감
        product.decreaseStock(request.getQuantity());

        // 포인트 차감
        if (usePoints > 0) {
            member.decreasePointBalance(usePoints);
        }

        // 쿠폰 무효화
        if (memberCoupon != null) {
            memberCoupon.useCoupon(order);
        }
        return new OrderResponse(order,List.of(orderItem));
    }

    // 주문 생성 (장바구니 구매)
    @Transactional
    public OrderResponse createOrder(Long memberId, CreateOrderRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 장바구니 존재 확인
        Cart cart = cartRepository.findByMemberId(memberId).orElseThrow(
                () -> new CartException(CartErrorCode.CART_NOT_FOUND)
        );
        // 장바구니 상품 확인
        // 비관락 도입 하면서 findByCartWithProduct() -> findByCart() 변경
        List<CartItem> cartItems = cartItemRepository.findByCart(cart);
        if (cartItems.isEmpty()) {
            throw new CartException(CartErrorCode.CART_EMPTY);
        }

        List<Long> productIds = cartItems.stream()
                .map(item -> item.getProduct().getId())
                .sorted()
                .toList();

        List<Product> products = productRepository.findAllByIdWithLock(productIds);

        Map<Long, Product> productMap = products.stream()
                .collect(Collectors.toMap(Product::getId, p -> p));

        // 상품 재고 체크
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProduct().getId());
            product.validateStock(item.getQuantity());
        }
        // 총 주문 금액
        Long totalAmount = cartItems.stream()
                .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity())
                .sum();

        // 쿠폰 유효성 검증
        MemberCoupon memberCoupon = null;
        if (request.getMemberCouponId() != null) {
            memberCoupon = memberCouponRepository.findById(request.getMemberCouponId()).orElseThrow(
                    () -> new MemberCouponException(MemberCouponErrorCode.COUPON_NOT_FOUND)
            );
            memberCoupon.validateCoupon(memberId);
        }
        // 쿠폰 할인 금액
        Long useCouponPrice = memberCoupon != null ? memberCoupon.getCoupon().getDiscountAmount() : 0L;

        // 포인트 검증
        Long usePoints = request.getUsePoints() != null ? request.getUsePoints() : 0L;
        member.validatePoint(usePoints);

        // 주문금액 초과 체크
        if (usePoints > totalAmount) {
            throw new OrderException(OrderErrorCode.EXCEED_POINT_USAGE);
        }

        // 주문생성 (=결제완료)
        String orderNumber = Order.generateOrderNumber();

        Order order = orderRepository.save(Order.create(
                member,
                orderNumber,
                totalAmount,
                usePoints,
                useCouponPrice
        ));
        // 주문 상품 생성
        // 장바구니 상품 돌면서 상품이랑 수량을 리스트로 담기 -> 주문, 상품, 수량 의 리스트가 생김
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem item : cartItems) {
            Product product = productMap.get(item.getProduct().getId());
            // 재고 차감
            product.decreaseStock(item.getQuantity());
            // 주문 상품 생성
            OrderItem saveOrderItems = orderItemRepository.save(
                    OrderItem.create(order, item.getProduct(), item.getQuantity())
            );
            orderItems.add(saveOrderItems);
        }
        // 포인트 차감
        member.decreasePointBalance(usePoints);

        // 쿠폰 무효화
        if (memberCoupon != null) {
            memberCoupon.useCoupon(order);
        }
        // 장바구니 상품 삭제
        cartItemRepository.deleteAllInBatch(cartItems);

        return new OrderResponse(order, orderItems);
    }

    // 주문 목록 조회
    public List<GetOrderResponse> getOrders(Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );
        List<Order> orders = orderRepository.findByMemberId(memberId);

        // WHERE IN 으로 한 번에 모든 OrderItem 조회
        List<OrderItem> allOrderItems = orderItemRepository.findByOrderIn(orders);

        // Map 그룹핑 -> 각 주문상품들이 어드 주문에 들어가야 하는지 orderId로 같은 거 끼리 묶음
        Map<Long, List<OrderItem>> orderItemMap = allOrderItems.stream()
                .collect(Collectors.groupingBy(orderItem -> orderItem.getOrder().getId()));

        return orders.stream()
                .map(order -> {
                    // key가 있으면 주문상품 목록 반환, 없으면 빈 리스트 반환
                    List<OrderItem> orderItems = orderItemMap.getOrDefault(order.getId(), List.of());
                    return new GetOrderResponse(order, orderItems);
                })
                .toList();
    }

    // 주문 상세 조회
    public OrderResponse getOneOrder(Long memberId, Long orderId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );
        // order 조회시 member도 같이 조회 -> N+1 방지
        Order order = orderRepository.findByIdWithMember(orderId).orElseThrow(
                () -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND)
        );
        // 본인 주문이 아니면 에러
        if (!order.getMember().getId().equals(memberId)) {
            throw new AuthException(CommonErrorCode.FORBIDDEN);
        }
        // fetch join으로 orderItem, product 한 번에 조회 -> N+1 방지
        List<OrderItem> orderItems = orderItemRepository.findByOrderWithProduct(order);

        return new OrderResponse(order, orderItems);
    }

    // 주문 취소 (환불)
    @Transactional
    public CancelOrderResponse cancelOrder(Long memberId, Long orderId, CancelOrderRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );
        // order 조회시 member도 같이 조회 -> N+1 방지
        Order order = orderRepository.findByIdWithMember(orderId).orElseThrow(
                () -> new OrderException(OrderErrorCode.ORDER_NOT_FOUND)
        );
        // 본인 주문인지
        if (!order.getMember().getId().equals(memberId)) {
            throw new AuthException(CommonErrorCode.FORBIDDEN);
        }
        // 이미 취소된 주문인지
        if (order.getStatus() == OrderStatus.CANCELED) {
            throw new OrderException(OrderErrorCode.ALREADY_CANCEL_ORDER);
        }
        // 주문 상태 변경
        order.cancel();

        // fetch join으로 orderItem, product 한 번에 조회 -> N+1 방지
        List<OrderItem> orderItems = orderItemRepository.findByOrderWithProduct(order);

        // 재고 복구
        for (OrderItem item : orderItems) {
            item.getProduct().increaseStock(item.getQuantity());
        }
        // 포인트 복구
        member.increasePointBalance(order.getUsedPoints());
        // 쿠폰 복구
        Optional<MemberCoupon> memberCoupon = memberCouponRepository.findByOrder(order);
        if (memberCoupon.isPresent()) {
            memberCoupon.get().restoreCoupon();
        }
        return new CancelOrderResponse(order);
    }
}
