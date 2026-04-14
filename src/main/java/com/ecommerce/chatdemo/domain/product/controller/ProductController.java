package com.ecommerce.chatdemo.domain.product.controller;

import com.ecommerce.chatdemo.domain.product.entity.response.ProductDetailResponse;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.ecommerce.chatdemo.domain.product.service.ProductService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class ProductController {
    private final ProductService service;


    @GetMapping("/categories/{categoryId}/products")
    public ResponseEntity<ApiResponse<List<ProductSummaryResponse>>> getProductsByCategory(
            @PathVariable Long categoryId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getProductsByCategory(categoryId)));
    }

    @GetMapping("/products/{productId}")
    public ResponseEntity<ApiResponse<ProductDetailResponse>> getProduct(
            @PathVariable Long productId
    ) {
        return ResponseEntity.ok(ApiResponse.success(service.getProduct(productId)));
    }
}
