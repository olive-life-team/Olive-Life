package com.ecommerce.chatdemo.global.exception;

public class AuthException extends BusinessException {

    public AuthException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AuthException(ErrorCode errorCode, Throwable cause) {
        super(errorCode, cause);
    }
}