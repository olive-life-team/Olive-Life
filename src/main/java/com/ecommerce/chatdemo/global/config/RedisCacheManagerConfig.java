package com.ecommerce.chatdemo.global.config;

import com.ecommerce.chatdemo.domain.product.entity.response.ProductSearchResult;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.time.Duration;

@Configuration
public class RedisCacheManagerConfig {


    public final static String CACHE_NAME = "redisCacheManager-searchCache";
    private static final long DURATION = 10;
    /**
     ObjectMapper : java <-> json 을 변환시켜주는 jackson 라이브러리 핵심 클래스 (Redis의 값을 저장하기 위해 필요!)
     */
    @Bean
    public CacheManager redisCacheManager(RedisConnectionFactory connectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);


        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(DURATION))   // ttl
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer())) //캐시 키를 어떻게 Redis에 저장할지?
                .serializeValuesWith( RedisSerializationContext.SerializationPair
                        .fromSerializer(new Jackson2JsonRedisSerializer<>(objectMapper, ProductSearchResult.class)));  // 캐시 값을 어떻게 Redis에 저장할지

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .build();
    }




}
