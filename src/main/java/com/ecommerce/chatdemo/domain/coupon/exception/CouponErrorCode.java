package com.ecommerce.chatdemo.domain.coupon.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CouponErrorCode implements ErrorCode {

    COUPON_OUT_OF_STOCK(HttpStatus.BAD_REQUEST, "COUPON_001", "쿠폰이 모두 소진되었습니다."),
    INVALID_ISSUE_DATE(HttpStatus.BAD_REQUEST, "COUPON_002", "발급 종료일은 발급 시작일 이후여야 합니다."),
    INVALID_USE_DATE(HttpStatus.BAD_REQUEST, "COUPON_003", "사용 종료일은 사용 시작일 이후여야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
