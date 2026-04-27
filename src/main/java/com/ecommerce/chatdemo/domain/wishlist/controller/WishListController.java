package com.ecommerce.chatdemo.domain.wishlist.controller;

import com.ecommerce.chatdemo.domain.wishlist.dto.MessageResponse;
import com.ecommerce.chatdemo.domain.wishlist.dto.WishListResponse;
import com.ecommerce.chatdemo.domain.wishlist.service.WishListService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
public class WishListController {

    private final WishListService wishListService;

    /**
     * 찜 목록 조회
     * @param loginUserInfo
     * @param page
     * @param size
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<Page<WishListResponse>>> getWishlist(
            @LoginUser LoginUserInfo loginUserInfo,
            @RequestParam(required = false, defaultValue = "1") int page,
            @RequestParam(required = false, defaultValue = "10") int size) {
        return ResponseEntity.ok(ApiResponse.success(wishListService.getWishlist(loginUserInfo.id(), page, size)));
    }

    /**
     * 찜 추가
     * @param loginUserInfo
     * @param productId
     * @return
     */
    @PostMapping("/{productId}")
    public ResponseEntity<ApiResponse<WishListResponse>> createWishlist(
            @LoginUser LoginUserInfo loginUserInfo,
            @PathVariable Long productId) {
        return ResponseEntity.ok(ApiResponse.success(wishListService.createWishlist(loginUserInfo.id(), productId)));
    }

    /**
     * 찜 삭제
     * @param productId
     * @return
     */
    @DeleteMapping("/{productId}")
    public ResponseEntity<ApiResponse<MessageResponse>> deleteWishlist(
            @PathVariable Long productId) {
        wishListService.deleteWishlist(productId);
        return ResponseEntity.ok(ApiResponse.success(new MessageResponse("찜한 상품이 삭제되었습니다")));
    }
}
