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
        if (!lockRedisRepository.tryLock(lockKey, lockValue, 2)) {
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

        while (retry < 8) {
            // 락 획득 시도
            if (lockRedisRepository.tryLock(lockKey, lockValue, 2)) {
                try {
                    return couponService.issueCoupon(memberId, couponId);
                } finally {
                    lockRedisRepository.releaseLock(lockKey, lockValue);
                }
            }
            // 실패시 70ms 대기 후 재시도
            // 70ms * 8번 = 560ms -> TTL 2초 안에 충분히 재시도 가능
            retry++;
            try {
                Thread.sleep(70);
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

        // 최대 대기 시간 설정 (TTL과 동일하게)
        long deadLine = System.currentTimeMillis() + 2000;

        // 최대 대기 시간 내에서 락이 해제될 때까지 계속 시도
        while (System.currentTimeMillis() < deadLine) {
            if (lockRedisRepository.tryLock(lockKey, lockValue, 2)) {
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
        // 최대 대기 시간 초과 시 예외 반환
        throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
    }
}
