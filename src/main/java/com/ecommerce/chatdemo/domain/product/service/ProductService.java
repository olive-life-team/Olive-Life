package com.ecommerce.chatdemo.domain.product.service;

import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductDetailResponse;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository repository;

    public List<ProductSummaryResponse> getProductsByCategory(Long categoryId) {
        List<Product> products = repository.findByCategoryId(categoryId);
        return products.stream()
                .map(product -> ProductSummaryResponse.from(product))
                .toList();
    }

    public ProductDetailResponse getProduct(Long productId) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.NOT_FOUND));
        return ProductDetailResponse.from(product);
    }
}
