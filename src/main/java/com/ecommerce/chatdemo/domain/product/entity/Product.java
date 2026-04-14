package com.ecommerce.chatdemo.domain.product.entity;

import com.ecommerce.chatdemo.domain.category.entity.Category;
import com.ecommerce.chatdemo.domain.product.exception.ProductErrorCode;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


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

    @Column(nullable = false)
    private Long price;

    @Column(nullable = false)
    private Long stock;

    @Column(length = 255)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ProductStatus status;

    @Column(nullable = false)
    private Long views;

    @Builder
    private Product(
            Category category,
            String name,
            Long price,
            Long stock,
            String description,
            ProductStatus status,
            Long views
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
            Long price,
            Long stock,
            String description
    ) {
        return Product.builder()
                .category(category)
                .name(name)
                .price(price)
                .stock(stock)
                .description(description)
                .status(resolveStatus(stock))
                .views(0L)
                .build();
    }

    private static ProductStatus resolveStatus(Long stock) {
        if (stock == null || stock <= 0) {
            return ProductStatus.OUT_OF_STOCK;
        }
        return ProductStatus.ON_SALE;
    }

    // 재고 체크 메서드
    public void validateStock(Integer quantity) {
        if (this.stock < quantity) {
            throw new BusinessException(ProductErrorCode.PRODUCT_OUT_OF_STOCK);
        }
    }

    // 재고 차감 메서드
    public void decreaseStock(Integer quantity) {
        this.stock -= quantity;
    }

    // 재고 복구 메서드
    public void increaseStock(Integer quantity) {
        this.stock += quantity;
    }
}