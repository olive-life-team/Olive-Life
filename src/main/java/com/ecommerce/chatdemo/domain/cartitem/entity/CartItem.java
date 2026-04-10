package com.ecommerce.chatdemo.domain.cartitem.entity;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false, precision = 15, scale = 0)
    private BigDecimal productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private CartItem(
            Cart cart,
            Product product,
            String productName,
            BigDecimal productPrice,
            Integer quantity
    ) {
        this.cart = cart;
        this.product = product;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }

    public static CartItem create(Cart cart, Product product, Integer quantity) {
        return CartItem.builder()
                .cart(cart)
                .product(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(quantity)
                .build();
    }
}