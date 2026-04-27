package com.ecommerce.chatdemo.domain.product.entity.response;


import com.ecommerce.chatdemo.domain.product.entity.Product;

public record ProductDetailResponse(
        Long id,
        String name,
        Long price,
        String description,
        String status,
        Long stock,
        Long views,
        String categoryName
) {
    public static ProductDetailResponse from(Product product) {
        return new ProductDetailResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getDescription(),
                product.getStatus().name(),
                product.getStock(),
                product.getViews(),
                product.getCategory().getName()
        );
    }
}
