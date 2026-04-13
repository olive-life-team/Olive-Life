package com.ecommerce.chatdemo.global.security.jwt;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenBlacklistService {

    private static final String BLACKLIST_PREFIX = "blacklist:";

    private final StringRedisTemplate stringRedisTemplate;

    public void blacklist(String token, long remainingMillis) {
        if (remainingMillis <= 0) {
            return;
        }

        stringRedisTemplate.opsForValue()
                .set(BLACKLIST_PREFIX + token, "logout", Duration.ofMillis(remainingMillis));
    }

    public boolean isBlacklisted(String token) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(BLACKLIST_PREFIX + token));
    }
}