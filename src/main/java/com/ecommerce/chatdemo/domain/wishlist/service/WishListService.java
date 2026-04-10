package com.ecommerce.chatdemo.domain.wishlist.service;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
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
    private final MemberRepository memberRepository;
    private final ProductRepository productRepository;

    // 찜 목록 조회 - 본인 찜 목록만 조회
    @Transactional(readOnly = true)
    public Page<WishListResponse> getWishlist(Long memberId, int page, int size) {

        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<WishList> wishlist = wishListRepository.findByMemberId(memberId, pageable);

        return wishlist.map(WishListResponse::new);
    }

    // 찜 추가
    @Transactional
    public WishListResponse createWishlist(Long memberId, Long productId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("없는 유저입니다.")
        );

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new IllegalArgumentException("없는 상품입니다.")
        );

        WishList wishlist = WishList.builder()
                .member(member)
                .product(product)
                .build();

        wishListRepository.save(wishlist);

        return new WishListResponse(wishlist);
    }
}
