package com.ecommerce.chatdemo.domain.order.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class DirectOrderRequest {

    @NotNull
    private Long productId;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;

    private Long memberCouponId;
    private Long usePoints;
}
