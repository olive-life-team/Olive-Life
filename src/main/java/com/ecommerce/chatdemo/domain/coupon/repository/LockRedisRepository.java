package com.ecommerce.chatdemo.domain.coupon.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
public class LockRedisRepository {

    private final StringRedisTemplate redisTemplate;

    // 락 획득 시도
    // 처리중인 키가 없으면 락 획득 성공 -> true
    // 이미 처리중인 키가 있으면 락 획득 실패 -> false
    public boolean tryLock(String key, String value, long timeoutSeconds) {
        return Boolean.TRUE.equals( // null-safe 처리
                redisTemplate.opsForValue()
                        .setIfAbsent(key, value, timeoutSeconds, TimeUnit.SECONDS)
                // SET NX EX (없으면 세팅해라)
        );
    }

    // 락 해제 (Lua 스크립트로 원자적으로 처리)
    // 본인이 건 락인지 UUID 확인 후 삭제
    // 락이 이미 만료되어 다른 서버가 새 락을 가져간 경우에는 삭제하지 않음
    public void releaseLock(String key, String value) {
        // get + del을 한번에 원자적 처리
        String script = """
                if redis.call('get',    KEYS[1]) == ARGV[1] then
                    return redis.call('del', KEYS[1])
                else
                    return 0
                end
                """;
        redisTemplate.execute(
                new DefaultRedisScript<>(script, Long.class),
                List.of(key),   // KEYS[1]
                value           // ARGV[1]
        );
    }
}
