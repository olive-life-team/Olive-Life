package com.ecommerce.chatdemo.domain.wishlist.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class WishListException extends BusinessException {
    public WishListException(ErrorCode errorCode) {
        super(errorCode);
    }
}
