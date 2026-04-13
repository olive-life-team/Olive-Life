package com.ecommerce.chatdemo.domain.wishlist.exception;

import com.ecommerce.chatdemo.global.exception.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum WishListErrorCode implements ErrorCode {
    WISHLIST_ALREADY_EXISTS(HttpStatus.CONFLICT, "WISHLIST_001", "이미 찜한 상품입니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;
}
