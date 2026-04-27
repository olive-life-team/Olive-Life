package com.ecommerce.chatdemo.domain.coupon.service;

import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CouponCoreService {

    private final CouponService couponService;

    public void issueCouponRetry(Long memberId, Long couponId) {
        int retry = 0;
        while (retry < 5) {
            try {
                couponService.issueCouponWithOptimisticLock(memberId, couponId);
                return;
            } catch (OptimisticLockingFailureException e) {
                retry++;
                System.out.println("충돌 발생, 재시도: " + retry);
                try {
                    Thread.sleep(70);
                }catch (InterruptedException ignored) {
                }
            }
        }
        throw new CouponException(CouponErrorCode.COUPON_ISSUE_FAILED);
    }
}
