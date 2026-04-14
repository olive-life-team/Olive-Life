package com.ecommerce.chatdemo.domain.order.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CancelOrderRequest {

    @NotNull
    private String cancelReason;
}
