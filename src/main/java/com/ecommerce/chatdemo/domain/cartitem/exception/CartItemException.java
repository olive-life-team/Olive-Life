package com.ecommerce.chatdemo.domain.cartitem.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class CartItemException extends BusinessException {
    public CartItemException(ErrorCode errorCode) {
        super(errorCode);
    }
}
