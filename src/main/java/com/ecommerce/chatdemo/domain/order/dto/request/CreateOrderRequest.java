package com.ecommerce.chatdemo.domain.order.dto.request;

import lombok.Getter;

@Getter
public class CreateOrderRequest {

    private Long memberCouponId;
    private Long usePoints;
}
