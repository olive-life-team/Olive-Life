package com.ecommerce.chatdemo.domain.cartitem.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartItemErrorCode implements ErrorCode {

    INVALID_QUANTITY(HttpStatus.BAD_REQUEST, "CARTITEM_001", "수량은 1개 이상이어야 합니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
