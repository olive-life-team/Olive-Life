package com.ecommerce.chatdemo.domain.coupon.controller;

import com.ecommerce.chatdemo.domain.coupon.dto.request.CreateCouponRequest;
import com.ecommerce.chatdemo.domain.coupon.dto.response.CreateCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.GetMemberCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.dto.response.IssueCouponResponse;
import com.ecommerce.chatdemo.domain.coupon.service.CouponService;
import com.ecommerce.chatdemo.domain.coupon.service.LockService;
import com.ecommerce.chatdemo.domain.coupon.service.NamedLockService;
import com.ecommerce.chatdemo.domain.coupon.service.RedissonService;
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
@RequestMapping("/api/coupons")
public class CouponController {

    private final CouponService couponService;
    private final LockService lockService;
    private final NamedLockService namedLockService;
    private final RedissonService redissonService;

    // 관리자가 쿠폰 생성
    @PostMapping("/admin")
    public ResponseEntity<ApiResponse<CreateCouponResponse>> createCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @Valid @RequestBody CreateCouponRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.createCoupon(loginUserInfo.id(), request)));
    }

    // 관리자가 쿠폰 목록 조회
    @GetMapping("/admin")
    public ResponseEntity<ApiResponse<Page<GetCouponResponse>>> getCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getCoupon(loginUserInfo.id(), page, size)));
    }

    // 사용자가 쿠폰 목록 조회
    @GetMapping
    public ResponseEntity<ApiResponse<Page<GetMemberCouponResponse>>> getMemberCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size
    ) {
        return ResponseEntity.ok(ApiResponse.success(couponService.getMemberCoupon(loginUserInfo.id(), page, size)));
    }

    // 사용자가 쿠폰 등록
    // 락 X
    @PostMapping("/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCoupon(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.issueCoupon(loginUserInfo.id(), couponId)));
    }

    // 낙관락 버전
    @PostMapping("/v2/{couponId}/issue/")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithOptimisticLock(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.issueCouponWithOptimisticLock(loginUserInfo.id(), couponId)));
    }

    // 비관적 락 버전
    @PostMapping("/v3/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithPessimisticLock(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(couponService.issueCouponWithPessimisticLock(loginUserInfo.id(), couponId)));
    }

    // mysql 락 버전 (네임드 락)
    @PostMapping("/v4/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithNamedLock(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(namedLockService.issueCouponWithNamedLock(loginUserInfo.id(), couponId)));
    }

    // redis 분산 락 (Fail-Fast)
    @PostMapping("/redis/v1/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithRedisLock(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lockService.issueCouponWithRedisLock(loginUserInfo.id(), couponId)));
    }

    // redis 분산 락 (Retry With BackOff)
    @PostMapping("/redis/v2/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithRedisLockWithRetry(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lockService.issueCouponWithRedisLockWithRetry(loginUserInfo.id(), couponId)));
    }

    // redis 분산 락 (Blocking)
    @PostMapping("/redis/v3/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithRedisLockWithBlocking(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(lockService.issueCouponWithRedisLockWithBlocking(loginUserInfo.id(), couponId)));
    }

    // Redisson 적용
    @PostMapping("/redisson/{couponId}/issue")
    public ResponseEntity<ApiResponse<IssueCouponResponse>> issueCouponWithRedisson(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long couponId
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(redissonService.issueCouponWithRedisson(loginUserInfo.id(), couponId)));
    }
}
