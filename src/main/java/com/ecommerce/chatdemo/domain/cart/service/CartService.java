package com.ecommerce.chatdemo.domain.cart.service;

import com.ecommerce.chatdemo.domain.cart.dto.request.CreateCartRequest;
import com.ecommerce.chatdemo.domain.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.chatdemo.domain.cart.dto.response.CreateCartResponse;
import com.ecommerce.chatdemo.domain.cart.dto.response.GetCartResponse;
import com.ecommerce.chatdemo.domain.cart.dto.response.UpdateCartItemResponse;
import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cart.repository.CartRepository;
import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import com.ecommerce.chatdemo.domain.cartitem.exception.CartItemErrorCode;
import com.ecommerce.chatdemo.domain.cartitem.repository.CartItemRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
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
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;
    private final CartItemRepository cartItemRepository;

    // 장바구니 상품 추가
    @Transactional
    public CreateCartResponse createCart(Long memberId, CreateCartRequest request) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new BusinessException(AuthErrorCode.USER_NOT_FOUND)
        );
        // 해당 사용자의 카트가 있는 지 확인. 없다면 새로 생성해서 저장
        Cart cart = cartRepository.findByMemberId(memberId).orElseGet(
                () -> cartRepository.save(Cart.create(member)));

        // 상품이 존재하는 지 확인.
        Product product = productRepository.findById(request.getProductId()).orElseThrow(
                () -> new BusinessException(ProductErrorCode.PRODUCT_NOT_FOUND)
        );

        // 이미 담긴 상품인지 확인 (중복체크)
        Optional<CartItem> existCartItem = cartItemRepository.findByCartAndProduct(cart, product);

        int totalQuantity = request.getQuantity();
        CartItem savedCartItem;

        // 이미 장바구니에 담긴 상품 추가 할 시
        if (existCartItem.isPresent()) {
            CartItem item = existCartItem.get();

            // 재고 체크
            totalQuantity += item.getQuantity();
            product.validateStock(totalQuantity);

            // 수량 증가 (Dirty Checking)
            item.addQuantity(request.getQuantity());
            savedCartItem = item;
        } else {
            // 새로운 상품 추가할 시
            // 재고 체크
            product.validateStock(totalQuantity);

            // 새로 생성 및 저장
            savedCartItem = cartItemRepository.save(CartItem.create(cart, product, request.getQuantity()));
        }
        return new CreateCartResponse(
                savedCartItem.getId(),
                product.getId(),
                product.getName(),
                product.getPrice(),
                savedCartItem.getQuantity()
        );
    }

    // 장바구니 목록 조회
    public GetCartResponse getCartItems(Long memberId) {
        // Cart 조회
        Optional<Cart> cart = cartRepository.findByMemberId(memberId);

        // Cart 없으면 빈 응답 반환
        if (cart.isEmpty()) {
            return new GetCartResponse();
        }

        // cartItem 있는 지 확인
        List<CartItem> cartItems = cartItemRepository.findByCartWithProduct(cart.get());
        return new GetCartResponse(cart.get(), cartItems);
    }

    // 장바구니 상품 수량 수정
    @Transactional
    public UpdateCartItemResponse updateCartItem(Long memberId, Long cartItemId, UpdateCartItemRequest request) {
        // 해당 장바구니 상품 조회
        CartItem cartItem = cartItemRepository.findByCartItemIdWithProduct(cartItemId).orElseThrow(
                () -> new BusinessException(CartItemErrorCode.CARTITEM_NOT_FOUND)
        );
        // 본인 장바구니 맞는 지
        if (!cartItem.getCart().getMember().getId().equals(memberId)) {
            throw new BusinessException(CommonErrorCode.FORBIDDEN);
        }
        // 변경하려는 수량이 0 이하면 에러
        if (request.getQuantity() <= 0) {
            throw new BusinessException(CartItemErrorCode.INVALID_QUANTITY);
        }
        // 재고 체크
        cartItem.getProduct().validateStock(request.getQuantity());

        // 수량 업데이트
        cartItem.updateQuantity(request.getQuantity());

        return new UpdateCartItemResponse(cartItem);
    }
}
