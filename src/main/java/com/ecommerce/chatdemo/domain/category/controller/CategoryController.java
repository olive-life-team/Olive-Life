package com.ecommerce.chatdemo.domain.category.controller;

import com.ecommerce.chatdemo.domain.category.dto.response.CategoryResponse;
import com.ecommerce.chatdemo.domain.category.service.CategoryService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(service.getCategories()));
    }
}
