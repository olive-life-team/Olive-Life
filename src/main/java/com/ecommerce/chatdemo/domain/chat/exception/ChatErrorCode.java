package com.ecommerce.chatdemo.domain.chat.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHATROOM_NOT_FOUND(HttpStatus.CONFLICT, "CHAT_001", "없는 채팅방입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
