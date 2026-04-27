package com.ecommerce.chatdemo.domain.product.entity.response;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.data.domain.Page;

import java.util.List;

@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public record ProductSearchResult(
        List<ProductSummaryResponse> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static ProductSearchResult from(Page<ProductSummaryResponse> page) {
        return new ProductSearchResult(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages()
        );
    }
}

