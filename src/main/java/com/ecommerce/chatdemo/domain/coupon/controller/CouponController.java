package com.ecommerce.chatdemo.domain.coupon.controller;

import com.ecommerce.chatdemo.domain.coupon.dto.request.CreateCouponRequest;
import com.ecommerce.chatdemo.domain.coupon.dto.response.CreateCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.service.CouponService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin/coupons")
public class CouponController {

    private final CouponService couponService;

    // 관리자가 쿠폰 생성
    @PostMapping
    public ResponseEntity<ApiResponse<CreateCouponResponse>> createCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @Valid @RequestBody CreateCouponRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.createCoupon(loginUserInfo.id(), request)));
    }

    // 관리자가 쿠폰 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetCouponResponse>>> getCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupon(loginUserInfo.id(), page, size)));
    }

}
