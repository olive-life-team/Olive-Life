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
    INVALID_USE_DATE(HttpStatus.BAD_REQUEST, "COUPON_003", "사용 종료일은 사용 시작일 이후여야 합니다."),

    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "COUPON_004", "존재하지 않는 쿠폰입니다."),
    ALREADY_ISSUED_COUPON(HttpStatus.BAD_REQUEST, "COUPON_005", "이미 발급받은 쿠폰입니다."),
    INVALID_COUPON_ISSUE_DATE(HttpStatus.BAD_REQUEST, "COUPON_006", "쿠폰 발급 기간이 아닙니다."),
    COUPON_ISSUE_FAILED(HttpStatus.CONFLICT, "COUPON_006", "쿠폰 등록에 실패했습니다. 잠시 후 다시 시도해주세요.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
