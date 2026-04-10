package com.ecommerce.chatdemo.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

    DUPLICATE_EMAIL(HttpStatus.BAD_REQUEST, "AUTH_001", "이미 사용 중인 이메일입니다."),
    DUPLICATE_NICKNAME(HttpStatus.BAD_REQUEST, "AUTH_002", "이미 사용 중인 닉네임입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "AUTH_003", "사용자를 찾을 수 없습니다."),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_004", "이메일 또는 비밀번호가 올바르지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

}