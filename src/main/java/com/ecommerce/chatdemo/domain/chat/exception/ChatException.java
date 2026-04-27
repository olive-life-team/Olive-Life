package com.ecommerce.chatdemo.domain.chat.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class ChatException extends BusinessException {
    public ChatException(ErrorCode errorCode) {
        super(errorCode);
    }
}
