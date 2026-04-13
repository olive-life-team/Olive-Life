package com.ecommerce.chatdemo.domain.pointtransaction.entity.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {

    INVALID_POINT(HttpStatus.BAD_REQUEST, "POINT_001", "사용할 포인트는 0보다 작을 수 없습니다."),
    INSUFFICIENT_POINTS(HttpStatus.BAD_REQUEST, "POINT_001", "포인트 잔액이 부족합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
