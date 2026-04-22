package com.ecommerce.chatdemo.domain.product.service;

import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import com.ecommerce.chatdemo.domain.product.entity.request.UpdateStockRequest;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductDetailResponse;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSearchResult;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.global.config.CaffeineCacheConfig;
import com.ecommerce.chatdemo.global.config.RedisCacheManagerConfig;
import com.ecommerce.chatdemo.global.config.CacheSyncSubscriberConfig;
import com.ecommerce.chatdemo.global.config.RedisTemplateConfig;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.CommonErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class ProductService {

    private final ProductRepository repository;

    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheManager caffeineCacheManager;

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


    @Transactional
    public void updateStock(Long productId, UpdateStockRequest request) {
        Product product = repository.findById(productId)
                .orElseThrow(() -> new BusinessException(CommonErrorCode.NOT_FOUND));
        product.updateStock(request.stock());

        clearRedisCache();
        clearV4RedisCache();

        redisTemplate.convertAndSend(CacheSyncSubscriberConfig.CHANNEL_NAME, "재고 업데이트 되었다. 캐시다지워라!!");
        log.info("[Pub/Sub] 재고 변경으로 캐시 무효화 - productId: {}", productId);
    }


    // v1
    public Page<ProductSummaryResponse> search(ProductSearchRequest request) {
        log.info("[v1] DB 조회 - keyword: {}", request.keyword());
        return repository.search(request);
    }


    // v2
    @Cacheable(
            value = CaffeineCacheConfig.V2_CACHE_NAME,
            key = "#request.keyword() + ':' + #request.page() + ':' + #request.size()",
            cacheManager = "caffeineCacheManager"
    )
    public Page<ProductSummaryResponse> searchInLocalCache(ProductSearchRequest request) {
        log.info("[v2] L1 캐시 MISS → DB 조회 - keyword: {}", request.keyword());
        return repository.search(request);
    }

    @CacheEvict(
            value = CaffeineCacheConfig.V2_CACHE_NAME,
            allEntries = true
    )
    public void clearLocalCache() {
        log.info("[v2] 로컬 캐시가 삭제되었습니다. {allEntries = true}");
    }


    // v3
    @Cacheable(
            value = RedisCacheManagerConfig.CACHE_NAME,
            key = "#request.keyword() + ':' + #request.page() + ':' + #request.size()",
            cacheManager = "redisCacheManager"
    )
    public ProductSearchResult searchInRedisCache(ProductSearchRequest request) {
        log.info("[v3] L2 캐시 MISS → DB 조회 - keyword: {}", request.keyword());
        return ProductSearchResult.from(repository.search(request));
    }

    @CacheEvict(value = RedisCacheManagerConfig.CACHE_NAME, allEntries = true, cacheManager = "redisCacheManager")
    public void clearRedisCache() {
        log.info("[v3] Reids 캐시가 삭제되었습니다.");
    }


    // v4 (LocalCache + RedisCache + DB)
    public ProductSearchResult searchInHybridCache(ProductSearchRequest request) {
        String cacheKey = request.keyword() + ":" + request.page() + ":" + request.size();

        // LocalCache
        Cache caffeineCache = caffeineCacheManager.getCache(CaffeineCacheConfig.V4_CACHE_NAME);
        if (caffeineCache != null) {
            Cache.ValueWrapper cached = caffeineCache.get(cacheKey);
            if (cached != null) {
                log.info("[v4] L1 캐시 HIT - key: {}", cacheKey);
                return (ProductSearchResult) cached.get();
            }
        }
        log.info("[v4] L1 캐시 MISS - key: {}", cacheKey);

        // RedisCache
        String redisKey = RedisTemplateConfig.CACHE_NAME + "::" + cacheKey;
        Object redisValue = redisTemplate.opsForValue().get(redisKey);
        if (redisValue instanceof ProductSearchResult redisResult) {
            if (caffeineCache != null) {
                log.info("[v4] L2 캐시 HIT - key: {}", redisKey);
                caffeineCache.put(cacheKey, redisResult);
            }
            return redisResult;
        }
        log.info("[v4] L2 캐시 MISS - key: {}", redisKey);

        // DB
        log.info("[v4] DB 조회");
        ProductSearchResult result = ProductSearchResult.from(repository.search(request));
        redisTemplate.opsForValue().set(redisKey, result, 10, TimeUnit.MINUTES);
        if (caffeineCache != null) {
            caffeineCache.put(cacheKey, result);
        }
        return result;
    }

    private void clearV4RedisCache() {
        Set<String> keys = redisTemplate.keys(RedisTemplateConfig.CACHE_NAME + "::*");
        if (!keys.isEmpty()) {
            redisTemplate.delete(keys);
            log.info("[v4] Reids 캐시가 삭제되었습니다.");
        }
    }

}
