package com.ecommerce.chatdemo.domain.cart.controller;

import com.ecommerce.chatdemo.domain.cart.dto.request.CreateCartRequest;
import com.ecommerce.chatdemo.domain.cart.dto.response.CreateCartResponse;
import com.ecommerce.chatdemo.domain.cart.service.CartService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/carts/items")
public class CartController {

    private final CartService cartService;

    // 장바구니 추가 (상품 추가)
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCartResponse>> createCart(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody CreateCartRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(cartService.createCart(loginUserInfo.id(), request)));
    }
}
