package com.ecommerce.chatdemo.domain.cart.dto.response;

import com.ecommerce.chatdemo.domain.cart.entity.Cart;
import com.ecommerce.chatdemo.domain.cartitem.entity.CartItem;
import lombok.Getter;

import java.util.Collections;
import java.util.List;

@Getter
public class GetCartResponse {

    private final Long id;
    private final List<CartItemDetail> cartItemDetail;
    private final Long totalPrice;

    @Getter
    public static class CartItemDetail {

        private final Long cartItemId;
        private final Long productId;
        private final String productName;
        private final Long price;
        private final Integer quantity;

        public CartItemDetail(CartItem cartItem) {
            this.cartItemId = cartItem.getId();
            this.productId = cartItem.getProduct().getId();
            this.productName = cartItem.getProduct().getName();
            this.price = cartItem.getProduct().getPrice();
            this.quantity = cartItem.getQuantity();
        }
    }

    // 카트가 있을때
    public GetCartResponse(Cart cart, List<CartItem> cartItems) {
        this.id = cart.getId();
        this.cartItemDetail = cartItems.stream().map(CartItemDetail::new).toList();
        this.totalPrice = cartItems.stream()
                .mapToLong(item -> item.getProduct().getPrice() * item.getQuantity()).sum();
    }
    // 카트가 없을때
    public GetCartResponse() {
        this.id = null;
        this.cartItemDetail = Collections.emptyList();
        this.totalPrice = 0L;
    }

}
