package com.ecommerce.chatdemo.domain.cart.dto.response;

import lombok.Getter;

@Getter
public class CreateCartResponse {

    private final Long id;
    private final Long productId;
    private final String productName;
    private final Long price;
    private final Integer quantity;

    public CreateCartResponse(Long id, Long productId, String productName, Long price, Integer quantity) {
        this.id = id;
        this.productId = productId;
        this.productName = productName;
        this.price = price;
        this.quantity = quantity;
    }
}
