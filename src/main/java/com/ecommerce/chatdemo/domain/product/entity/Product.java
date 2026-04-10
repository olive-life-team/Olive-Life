package com.ecommerce.chatdemo.domain.product.entity;

import com.ecommerce.chatdemo.domain.category.entity.Category;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "product")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "category_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Category category;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ProductStatus status;

    @Column(nullable = false)
    private Integer views;

    @Builder
    private Product(
            Category category,
            String name,
            BigDecimal price,
            Integer stock,
            String description,
            ProductStatus status,
            Integer views
    ) {
        this.category = category;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.description = description;
        this.status = status;
        this.views = views;
    }

    public static Product create(
            Category category,
            String name,
            BigDecimal price,
            Integer stock,
            String description
    ) {
        return Product.builder()
                .category(category)
                .name(name)
                .price(price)
                .stock(stock)
                .description(description)
                .status(resolveStatus(stock))
                .views(0)
                .build();
    }

    private static ProductStatus resolveStatus(Integer stock) {
        if (stock == null || stock <= 0) {
            return ProductStatus.OUT_OF_STOCK;
        }
        return ProductStatus.ON_SALE;
    }
}