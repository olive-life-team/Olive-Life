package com.ecommerce.chatdemo.domain.cartitem.entity;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cartitem.exception.CartItemErrorCode;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart_item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CartItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "cart_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Cart cart;

    @JoinColumn(name = "product_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private CartItem(
            Cart cart,
            Product product,
            Integer quantity
    ) {
        this.cart = cart;
        this.product = product;
        this.quantity = quantity;
    }

    public static CartItem create(Cart cart, Product product, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .quantity(quantity)
                .build();
    }

    // 이미 담긴 상품 또 담았을 경우 수량만 증가
    public void addQuantity(Integer addQuantity) {
        this.quantity += addQuantity;
    }

    // 장바구니 수량 수정
    public void updateQuantity(Integer newQuantity) {
        if (newQuantity <= 0) {
            throw new BusinessException(CartItemErrorCode.INVALID_QUANTITY);
        }
        this.quantity = newQuantity;
    }
}