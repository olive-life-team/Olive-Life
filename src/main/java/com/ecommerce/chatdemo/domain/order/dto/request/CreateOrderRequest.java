package com.ecommerce.chatdemo.domain.order.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CreateOrderRequest {

    private Long memberCouponId;
    private Long usePoints;
}
