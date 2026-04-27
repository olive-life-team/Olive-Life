package com.ecommerce.chatdemo.domain.cart.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class CreateCartRequest {

    @NotNull
    private Long productId;

    @Min(value = 1, message = "수량은 1개 이상이어야 합니다.")
    private Integer quantity;
}
