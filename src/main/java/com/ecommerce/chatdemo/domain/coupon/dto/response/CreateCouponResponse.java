package com.ecommerce.chatdemo.domain.coupon.dto.response;

import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import lombok.Getter;

@Getter
public class CreateCouponResponse {

    private final Long couponId;
    private final CouponStatus status;
    private final String message;

    public CreateCouponResponse(Long couponId, CouponStatus status, String message) {
        this.couponId = couponId;
        this.status = status;
        this.message = message;
    }
}
