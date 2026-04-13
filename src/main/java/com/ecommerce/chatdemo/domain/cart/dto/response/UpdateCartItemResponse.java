package com.ecommerce.chatdemo.domain.cart.dto.response;

import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import lombok.Getter;

@Getter
public class UpdateCartItemResponse {

    private final Long cartItemId;
    private final Long productId;
    private final String productName;
    private final Long price;
    private final Integer quantity;

    public UpdateCartItemResponse(CartItem cartItem) {
        this.cartItemId = cartItem.getId();
        this.productId = cartItem.getProduct().getId();
        this.productName = cartItem.getProduct().getName();
        this.price = cartItem.getProduct().getPrice();
        this.quantity = cartItem.getQuantity();
    }
}
