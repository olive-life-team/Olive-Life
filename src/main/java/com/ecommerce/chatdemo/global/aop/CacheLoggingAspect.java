package com.ecommerce.chatdemo.global.aop;

import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
@Order(1)   //@Cacheable의 CacheInterceptor가  캐시 HIT이면 메서드 체인을 조기 종료가 되버려 order 지정이 반드시 필요!
public class CacheLoggingAspect {

    private final CacheManager caffeineCacheManager;

    @Around("@annotation(cacheable)")
    public Object logCacheHit(ProceedingJoinPoint joinPoint,
                              org.springframework.cache.annotation.Cacheable cacheable) throws Throwable {
        log.info("=== AOP 진입 ===");
        Object[] args = joinPoint.getArgs();
        ProductSearchRequest request = (ProductSearchRequest) args[0];
        String key = request.keyword() + ":" + request.page() + ":" + request.size();

        Cache cache = caffeineCacheManager.getCache(cacheable.value()[0]);
        boolean isHit = cache != null && cache.get(key) != null;

        log.info("CacheLoggingAspect => [{}] 캐시 {} - key: {}", cacheable.value()[0], isHit ? "HIT" : "MISS", key);

        return joinPoint.proceed();
    }
}

