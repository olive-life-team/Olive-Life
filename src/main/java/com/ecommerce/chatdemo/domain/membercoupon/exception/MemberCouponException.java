package com.ecommerce.chatdemo.domain.membercoupon.exception;

import com.ecommerce.chatdemo.global.exception.BusinessException;
import com.ecommerce.chatdemo.global.exception.ErrorCode;

public class MemberCouponException extends BusinessException {
    public MemberCouponException(ErrorCode errorCode) {
        super(errorCode);
    }
}
