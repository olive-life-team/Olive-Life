package com.ecommerce.chatdemo.global.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CaffeineCacheConfig {

    public final static String V2_CACHE_NAME = "v2-searchCache";
    public final static String V4_CACHE_NAME = "v4-searchCache";
    private static final long MAXIMUM_SIZE = 1000;
    private static final long DURATION = 10;


    @Primary    // @Primary : @Cacheable 에서 Manager지정을 default로함.
    @Bean
    public CacheManager caffeineCacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager(V2_CACHE_NAME, V4_CACHE_NAME);
        cacheManager.setCaffeine(
                Caffeine.newBuilder()
                        .maximumSize(MAXIMUM_SIZE)
                        .expireAfterWrite(DURATION, TimeUnit.MINUTES)
                        .recordStats()
        );
        return cacheManager;
    }
}
