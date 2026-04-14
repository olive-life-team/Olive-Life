package com.ecommerce.chatdemo.domain.order.controller;

import com.ecommerce.chatdemo.domain.order.dto.request.CreateOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.request.DirectOrderRequest;
import com.ecommerce.chatdemo.domain.order.dto.response.OrderResponse;
import com.ecommerce.chatdemo.domain.order.dto.response.GetOrderResponse;
import com.ecommerce.chatdemo.domain.order.service.OrderService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    // 즉시 결제(장바구니X)
    @PostMapping("/direct")
    public ResponseEntity<ApiResponse<OrderResponse>> createDirectOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody DirectOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createDirectOrder(loginUserInfo.id(), request)));
    }

    // 주문 생성(장바구니 구매)
    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestBody CreateOrderRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(orderService.createOrder(loginUserInfo.id(), request)));
    }

    // 주문 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<List<GetOrderResponse>>> getOrders(
            @LoginUser LoginUserInfo loginUserInfo
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOrders(loginUserInfo.id())));
    }

    // 주문 상세 조회
    @GetMapping("/{orderId}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOneOrder(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(ApiResponse.success(orderService.getOneOrder(loginUserInfo.id(), orderId)));
    }
}
