package com.ecommerce.chatdemo.domain.wishlist.service;

import com.ecommerce.chatdemo.domain.wishlist.dto.WishListResponse;
import com.ecommerce.chatdemo.domain.wishlist.entity.WishList;
import com.ecommerce.chatdemo.domain.wishlist.repository.WishListRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class WishListService {

    private final WishListRepository wishListRepository;

    // 찜 목록 조회 - 본인 찜 목록만 조회
    @Transactional(readOnly = true)
    public Page<WishListResponse> getWishlist(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WishList> wishlist = wishListRepository.findByMemberId(memberId, pageable);

        return wishlist.map(WishListResponse::new);
    }
}
