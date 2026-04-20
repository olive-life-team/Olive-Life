package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.dto.response.IssueCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
import com.ecommerce.chatdemo.domain.coupon.repository.NamedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class NamedLockService {

    private final NamedLockRepository namedLockRepository;
    private final CouponService couponService;

    // NamedLock
    public IssueCouponResponse issueCouponWithNamedLock(Long memberId, Long couponId) {
        String lockName = "coupon: " + couponId;

        try {
            // 락 획득 시도
            Integer result = namedLockRepository.getLock(lockName, 3);

            // result = null || result ! = 1 이라는 건, 락 획득 못했다 -> 이미 처리중인 락이 있다는 것
            if (result == null || result != 1) {
                throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
            }
            // result = 1 이 된다면 처리중인 락이 없어서 락을 획득하는데 성공
            return couponService.issueCoupon(memberId, couponId);
        } finally {
            namedLockRepository.releaseLock(lockName);
        }
    }
}
