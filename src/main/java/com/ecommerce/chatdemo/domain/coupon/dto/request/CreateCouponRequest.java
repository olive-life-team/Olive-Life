package com.ecommerce.chatdemo.domain.coupon.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CreateCouponRequest {

    @NotBlank
    private String couponName;

    @Min(value = 1000, message = "할인 금액은 1000원 이상이어야 합니다.")
    private Long discountAmount;

    @Min(value = 1, message = "쿠폰 수량은 1개 이상 발급되어야 합니다.")
    private Integer quantity;

    @FutureOrPresent(message = "발급 시작일은 과거일 수 없습니다.")
    private LocalDateTime issueStartAt;

    @FutureOrPresent(message = "발급 종료일은 과거일 수 없습니다.")
    private LocalDateTime issueEndAt;

    @FutureOrPresent(message = "사용 시작일은 과거일 수 없습니다.")
    private LocalDateTime useStartAt;

    @FutureOrPresent(message = "사용 종료일은 과거일 수 없습니다.")
    private LocalDateTime useEndAt;
}
