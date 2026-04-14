package com.ecommerce.chatdemo.domain.cart.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum CartErrorCode implements ErrorCode {

    CART_NOT_FOUND(HttpStatus.NOT_FOUND, "CART_001", "장바구니가 존재하지 않습니다."),
    CART_EMPTY(HttpStatus.NOT_FOUND, "CART_002", "장바구니가 비어있습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
