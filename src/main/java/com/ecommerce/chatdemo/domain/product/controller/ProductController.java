package com.ecommerce.chatdemo.domain.product.controller;

import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductDetailResponse;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.ecommerce.chatdemo.domain.product.service.ProductService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/products/v1/search")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> search(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ProductSearchRequest request = new ProductSearchRequest(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(service.search(request)));
    }

    @GetMapping("/products/v2/search")
    public ResponseEntity<ApiResponse<Page<ProductSummaryResponse>>> searchInLocalCache(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        ProductSearchRequest request = new ProductSearchRequest(keyword, page, size);
        return ResponseEntity.ok(ApiResponse.success(service.searchInLocalCache(request)));
    }

    @DeleteMapping("/products/cache")
    public ResponseEntity<ApiResponse<String>> clearLocalCache() {
        service.clearLocalCache();
        return ResponseEntity.ok(ApiResponse.success("로컬 캐시 삭제 완료"));
    }
}
