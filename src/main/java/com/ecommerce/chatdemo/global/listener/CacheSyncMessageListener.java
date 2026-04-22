package com.ecommerce.chatdemo.global.listener;

import com.ecommerce.chatdemo.global.config.CaffeineCacheConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CacheSyncMessageListener implements MessageListener {
    private final CacheManager caffeineCacheManager;

    @Override
    public void onMessage(Message message, byte[] pattern) {
        String body = new String(message.getBody());
        log.info("[CacheSyncMessageListener::onMessage] 캐시 동기화 메시지 수신 { message: {}", body);
        Cache v2Cache = caffeineCacheManager.getCache(CaffeineCacheConfig.V2_CACHE_NAME);
        Cache v4Cache = caffeineCacheManager.getCache(CaffeineCacheConfig.V4_CACHE_NAME);
        if (v2Cache != null) v2Cache.clear();
        if (v4Cache != null) v4Cache.clear();
    }
}
