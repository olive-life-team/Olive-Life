package com.ecommerce.chatdemo.domain.cart.dto.request;

import lombok.Getter;

@Getter
public class CreateCartRequest {

    private Long productId;
    private Integer quantity;
}
