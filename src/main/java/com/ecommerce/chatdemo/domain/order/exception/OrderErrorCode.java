package com.ecommerce.chatdemo.domain.order.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

    INVALID_FINAL_AMOUNT(HttpStatus.BAD_REQUEST, "ORDER_001", "최종 결제금액은 0보다 작을 수 없습니다."),
    EXCEED_POINT_USAGE(HttpStatus.BAD_REQUEST, "ORDER_002", "포인트 사용금액이 주문 금액을 초과할 수 없습니다."),
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "ORDER_003", "존재하지 않는 주문입니다."),
    ALREADY_CANCEL_ORDER(HttpStatus.BAD_REQUEST, "ORDER_004", "이미 취소된 주문입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
