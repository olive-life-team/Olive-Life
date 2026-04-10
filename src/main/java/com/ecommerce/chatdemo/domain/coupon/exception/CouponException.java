package com.ecommerce.chatdemo.domain.coupon.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class CouponException extends BusinessException {
    public CouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}
