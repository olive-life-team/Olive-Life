package com.ecommerce.chatdemo.domain.cart.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class CartException extends BusinessException {
    public CartException(ErrorCode errorCode) {
        super(errorCode);
    }
}
