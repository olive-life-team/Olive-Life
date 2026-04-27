package com.ecommerce.chatdemo.domain.member.dto;

import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCouponStatus;

import java.time.LocalDateTime;
import java.util.List;

public record MyInfoResponse(
        Long id,
        String email,
        String name,
        String role,
        Long pointBalance,
        String membershipName,
        List<CouponInfo> coupons
) {
    public record CouponInfo(
            Long memberCouponId,
            String couponName,
            Long discountAmount,
            MemberCouponStatus status,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
    }
}
