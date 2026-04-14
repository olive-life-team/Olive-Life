package com.ecommerce.chatdemo.domain.product.entity.response;

import com.ecommerce.chatdemo.domain.product.entity.Product;

public record ProductSummaryResponse(
        Long id,
        String name,
        Long price,
        String status,
        Long stock
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus().name(),
                product.getStock()
        );
    }
}
