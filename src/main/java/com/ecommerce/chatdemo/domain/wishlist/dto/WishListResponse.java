package com.ecommerce.chatdemo.domain.wishlist.dto;

import com.ecommerce.chatdemo.domain.wishlist.entity.WishList;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class WishListResponse {
    private Long id;
    private Long memberId;
    private Long productId;
    private String productName;
    private Long productPrice;
    private LocalDateTime createdAt;

    public WishListResponse(WishList wishList) {
        this.id = wishList.getId();
        this.memberId = wishList.getMember().getId();
        this.productId = wishList.getProduct().getId();
        this.productName = wishList.getProduct().getName();
        this.productPrice = wishList.getProduct().getPrice();
        this.createdAt = wishList.getCreatedAt();
    }
}
