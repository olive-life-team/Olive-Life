package com.ecommerce.chatdemo.domain.coupon.dto.response;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.coupon.entity.CouponStatus;
import lombok.Getter;

@Getter
public class GetCouponResponse {

    private final Long id;
    private final String couponName;
    private final Long discountAmount;
    private final Integer quantity;
    private final CouponStatus couponStatus;

    public GetCouponResponse(Coupon coupon) {
        this.id = coupon.getId();
        this.couponName = coupon.getName();
        this.discountAmount = coupon.getDiscountAmount();
        this.quantity = coupon.getQuantity();
        this.couponStatus = coupon.getStatus();
    }
}
