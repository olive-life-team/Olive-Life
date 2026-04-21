package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.dto.response.IssueCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
import com.ecommerce.chatdemo.global.exception.LockAcquisitionException;
import lombok.RequiredArgsConstructor;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedissonService {

    private final CouponService couponService;
    private final RedissonClient redissonClient;

    public IssueCouponResponse issueCouponWithRedisson(Long memberId, Long couponId) {
        // RLock 객체 생성
        // getLock()은 Redis에 접근X
        // 키 이름과 연결된 락 객체만 반환
        // Redis 명령은 tryLock()시점에 발행
        String lockKey = "lock:coupon:" + couponId;
        RLock lock = redissonClient.getLock(lockKey);

        // 락 획득 추적
        boolean acquired = false;

        try {
            // waitTime = 2초 : 락 획득 실패시 2초 대기 후 재시도 (Pub/Sub)
            // leaseTime = -1 : watchDog 활성화 -> 락 자동 갱신. if, 서버가 죽어도 와치독이 같이 중단(30초 뒤 락 만료)
            acquired = lock.tryLock(2, -1, TimeUnit.SECONDS);

            // 락을 획득하지 못했다면 에러 (그대로 acquired = false)
            if (!acquired) {
                throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
            }
            // 락을 획득했다면 (acquired = true)
            return couponService.issueCoupon(memberId, couponId);
        }catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LockAcquisitionException("락 대기 중 인터럽트가 발생했습니다.");
        } finally {
            // acquired = true이고 현재 스레드가 락 보유자인 경우에만 락 해제
            // isHeldByCurrentThread()로 다른 스레드 락 실수 해제 방지
            if (acquired && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }
}
