package com.ecommerce.chatdemo.domain.product.service;

import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductDetailResponse;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSearchResult;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.global.config.CaffeineCacheConfig;
import com.ecommerce.chatdemo.global.config.RedisCacheConfig;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final static String CACHE_NAME = "searchCache";
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


    // v1
    public Page<ProductSummaryResponse> search(ProductSearchRequest request) {
        return repository.search(request);
    }


    // v2
    @Cacheable(
            value = CaffeineCacheConfig.CACHE_NAME,
            key = "#request.keyword() + ':' + #request.page() + ':' + #request.size()",
            cacheManager = "caffeineCacheManager"
    )
    public Page<ProductSummaryResponse> searchInLocalCache(ProductSearchRequest request) {
        return repository.search(request);
    }

    @CacheEvict(
            value = CACHE_NAME,
            allEntries = true

    )
    public void clearLocalCache() {}


    // v3
    @Cacheable(
            value = RedisCacheConfig.CACHE_NAME,
            key = "#request.keyword() + ':' + #request.page() + ':' + #request.size()",
            cacheManager = "redisCacheManager"
    )
    public ProductSearchResult searchInRedisCache(ProductSearchRequest request) {
        return ProductSearchResult.from(repository.search(request));
    }
}
