package com.ecommerce.chatdemo.domain.coupon.dto.response;

import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCouponStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GetMemberCouponResponse {

    private final Long memberCouponId;
    private final String couponName;
    private final Long discountAmount;
    private final MemberCouponStatus status;
    private final LocalDateTime issuedAt;
    private final LocalDateTime useStartAt;
    private final LocalDateTime useEndAt;
    private final LocalDateTime expiredAt;

    public GetMemberCouponResponse(MemberCoupon memberCoupon) {
        this.memberCouponId = memberCoupon.getId();
        this.couponName = memberCoupon.getCoupon().getName();
        this.discountAmount = memberCoupon.getCoupon().getDiscountAmount();
        this.status = memberCoupon.getStatus();
        this.issuedAt = memberCoupon.getIssuedAt();
        this.useStartAt = memberCoupon.getCoupon().getUseStartAt();
        this.useEndAt = memberCoupon.getCoupon().getUseEndAt();
        this.expiredAt = memberCoupon.getExpiredAt();
    }
}
