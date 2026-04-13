package com.ecommerce.chatdemo.domain.membercoupon.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum MemberCouponErrorCode implements ErrorCode {

    EXPIRED_COUPON_DATETIME(HttpStatus.BAD_REQUEST, "MEMBERCOUPON_001", "만료된 쿠폰입니다."),
    ALREADY_USED_COUPON(HttpStatus.BAD_REQUEST, "MEMBERCOUPON_002", "이미 사용된 쿠폰입니다."),
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "MEMBERCOUPON_003", "존재하지 않는 쿠폰입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
