package com.ecommerce.chatdemo.domain.order.service;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.membercoupon.exception.MemberCouponErrorCode;
import com.ecommerce.chatdemo.domain.membercoupon.exception.MemberCouponException;
import com.ecommerce.chatdemo.domain.membercoupon.repository.MemberCouponRepository;
import com.ecommerce.chatdemo.domain.order.dto.request.DirectOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.response.DirectOrderResponse;
import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.order.repository.OrderRepository;
import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import com.ecommerce.chatdemo.domain.orderitem.entity.repository.OrderItemRepository;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.exception.ProductErrorCode;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.ecommerce.chatdemo.domain.membercoupon.entity.QMemberCoupon.memberCoupon;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrderService {

    private final OrderRepository orderRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final MemberCouponRepository memberCouponRepository;
    private final OrderItemRepository orderItemRepository;

    // 즉시 결제(장바구니X)
    @Transactional
    public DirectOrderResponse createDirectOrder(Long memberId, DirectOrderRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 요청 상품이 존재하는 지 확인
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
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
        member.decreasePointBalance(usePoints);

        // 쿠폰 무효화
        if (memberCoupon != null) {
            memberCoupon.useCoupon(order);
        }
        return new DirectOrderResponse(order,List.of(orderItem));
    }
}
