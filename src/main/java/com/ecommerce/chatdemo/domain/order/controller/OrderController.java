package com.ecommerce.chatdemo.domain.order.controller;

import com.ecommerce.chatdemo.domain.order.dto.request.DirectOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.response.DirectOrderResponse;
import com.ecommerce.chatdemo.domain.order.service.OrderService;
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
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 즉시 결제(장바구니X)
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<DirectOrderResponse>> createDirectOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody DirectOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createDirectOrder(loginUserInfo.id(), request)));
    }
}
