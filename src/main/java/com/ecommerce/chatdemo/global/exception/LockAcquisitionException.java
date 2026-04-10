package com.ecommerce.chatdemo.global.exception;

public class LockAcquisitionException extends RuntimeException {

    /**
     * Redis Lock 획득 실패를 공통 예외로 분리해두면
     * 나중에 분산 락을 실제로 붙였을 때 409 Conflict로 일관되게 처리할 수 있다.
     */
    public LockAcquisitionException(String message) {
        super(message);
    }
}
