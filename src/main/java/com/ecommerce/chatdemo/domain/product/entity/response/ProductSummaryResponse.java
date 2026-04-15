package com.ecommerce.chatdemo.domain.product.entity.response;

import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.entity.ProductStatus;

public record ProductSummaryResponse(
        Long id,
        String name,
        Long price,
        ProductStatus status,
        Long stock
) {
    public static ProductSummaryResponse from(Product product) {
        return new ProductSummaryResponse(
                product.getId(),
                product.getName(),
                product.getPrice(),
                product.getStatus(),
                product.getStock()
        );
    }
}
