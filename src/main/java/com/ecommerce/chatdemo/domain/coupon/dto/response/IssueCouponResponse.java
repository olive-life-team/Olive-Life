package com.ecommerce.chatdemo.domain.coupon.dto.response;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCouponStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class IssueCouponResponse {

    private final Long memberCouponId;
    private final Long memberId;
    private final Long couponId;
    private final String couponName;
    private final MemberCouponStatus status;
    private final LocalDateTime useStartAt;
    private final LocalDateTime useEndAt;

    public IssueCouponResponse(MemberCoupon memberCoupon) {
        this.memberCouponId = memberCoupon.getId();
        this.memberId = memberCoupon.getMember().getId();
        this.couponId = memberCoupon.getCoupon().getId();
        this.couponName = memberCoupon.getCoupon().getName();
        this.status = memberCoupon.getStatus();
        this.useStartAt = memberCoupon.getCoupon().getUseStartAt();
        this.useEndAt = memberCoupon.getCoupon().getUseEndAt();
    }
}
