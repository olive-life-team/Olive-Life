package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.dto.response.IssueCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
import com.ecommerce.chatdemo.domain.coupon.repository.LockRedisRepository;
import com.ecommerce.chatdemo.global.exception.LockAcquisitionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LockService {

    private final LockRedisRepository lockRedisRepository;
    private final CouponService couponService;

    // 1. Fail Fast 전략
    public IssueCouponResponse issueCouponWithRedisLock(Long memberId, Long couponId) {
        // 락, 키값 준비
        String lockKey = "lock:coupon:" + couponId;
        String lockValue = UUID.randomUUID().toString();

        // 락 획득 시도
        if (!lockRedisRepository.tryLock(lockKey, lockValue, 3)) {
            throw new LockAcquisitionException("잠시 후 다시 시도해 주세요.");
        }

        // 비지니스 로직
        try {
            return couponService.issueCoupon(memberId, couponId);
        } finally {
            lockRedisRepository.releaseLock(lockKey, lockValue);
        }
    }

    // 2. Retry 전략
    public IssueCouponResponse issueCouponWithRedisLockWithRetry(Long memberId, Long couponId) {
        // 락, 키값 준비
        String lockKey = "lock:coupon:" + couponId;
        String lockValue = UUID.randomUUID().toString();

        int retry = 0;

        while (retry < 10) {
            // 락 획득 시도
            if (lockRedisRepository.tryLock(lockKey, lockValue, 3)) {
                try {
                    return couponService.issueCoupon(memberId, couponId);
                } finally {
                    lockRedisRepository.releaseLock(lockKey, lockValue);
                }
            }
            // 실패시 50ms 대기 후 재시도
            // 50ms * 10번 = 500ms -> TTL 3초 안에 충분히 재시도 가능
            retry++;
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
            }
        }
        // 재시도 10번 넘어가면 에러
        throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
    }

    // 3. Blocking 전략 (Redisson으로 더 안전한 블로킹 구현 가능 -> waitTime)
    public IssueCouponResponse issueCouponWithRedisLockWithBlocking(Long memberId, Long couponId) {
        // 락, 키값 준비
        String lockKey = "lock:coupon:" + couponId;
        String lockValue = UUID.randomUUID().toString();

        // 락이 해제될 때까지 계속 시도
        while (true) {
            if (lockRedisRepository.tryLock(lockKey, lockValue, 3)) {
                try {
                    return couponService.issueCoupon(memberId, couponId);
                } finally {
                    lockRedisRepository.releaseLock(lockKey, lockValue);
                }
            }
            // 짧게 대기 후 재시도 (스핀락)
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
            }
        }
    }
}
