package com.ecommerce.chatdemo.domain.category.dto.response;

import com.ecommerce.chatdemo.domain.category.entity.Category;

public record CategoryResponse(
        Long id,
        String name,
        Long parentId
) {
    public static CategoryResponse from(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName(),
                category.getParentCategory() != null ? category.getParentCategory().getId() : null
        );
    }
}
