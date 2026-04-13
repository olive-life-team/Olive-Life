package com.ecommerce.chatdemo.domain.pointtransaction.entity.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class PointException extends BusinessException {
    public PointException(ErrorCode errorCode) {
        super(errorCode);
    }
}
