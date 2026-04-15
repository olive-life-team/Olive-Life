package com.ecommerce.chatdemo.domain.chat.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {
    CHATROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "없는 채팅방입니다."),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "CHAT_002", "현재 상태에서는 요청하신 상태로 변경할 수 없습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
