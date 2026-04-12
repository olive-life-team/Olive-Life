package com.ecommerce.chatdemo.domain.orderitem.entity;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @JoinColumn(name = "product_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Column(name = "product_name", nullable = false, length = 100)
    private String productName;

    @Column(name = "product_price", nullable = false, precision = 15, scale = 0)
    private Long productPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Builder
    private OrderItem(
            Order order,
            Product product,
            String productName,
            Long productPrice,
            Integer quantity
    ) {
        this.order = order;
        this.product = product;
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantity = quantity;
    }

    public static OrderItem create(Order order, Product product, Integer quantity) {
        return OrderItem.builder()
                .order(order)
                .product(product)
                .productName(product.getName())
                .productPrice(product.getPrice())
                .quantity(quantity)
                .build();
    }
}
