package com.ecommerce.chatdemo.domain.product.entity.request;

public record ProductSearchRequest(
        String keyword,
        int page,
        int size
) {
}
