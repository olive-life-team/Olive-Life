package com.ecommerce.chatdemo.domain.cart.controller;

import com.ecommerce.chatdemo.domain.cart.dto.request.CreateCartRequest;
import com.ecommerce.chatdemo.domain.cart.dto.request.UpdateCartItemRequest;
import com.ecommerce.chatdemo.domain.cart.dto.response.CreateCartResponse;
import com.ecommerce.chatdemo.domain.cart.dto.response.GetCartResponse;
import com.ecommerce.chatdemo.domain.cart.dto.response.UpdateCartItemResponse;
import com.ecommerce.chatdemo.domain.cart.service.CartService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cart/items")
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

    // 장바구니 상품 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<GetCartResponse>> getCartItems(
            @LoginUser LoginUserInfo loginUserInfo
    ) {
        return ResponseEntity.ok(ApiResponse.success(cartService.getCartItems(loginUserInfo.id())));
    }

    // 장바구니 상품 수량 수정
    @PatchMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<UpdateCartItemResponse>> updateCartItem(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long cartItemId,
            @RequestBody UpdateCartItemRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.success(cartService.updateCartItem(loginUserInfo.id(), cartItemId,request)));
    }

    // 장바구니 상품 삭제
    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long cartItemId
    ) {
        cartService.deleteCartItem(loginUserInfo.id(), cartItemId);
        return ResponseEntity.noContent().build();
    }
}
