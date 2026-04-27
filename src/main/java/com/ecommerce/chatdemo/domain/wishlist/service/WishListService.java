package com.ecommerce.chatdemo.domain.wishlist.service;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.domain.product.exception.ProductErrorCode;
import com.ecommerce.chatdemo.domain.product.exception.ProductException;
import com.ecommerce.chatdemo.domain.product.repository.ProductRepository;
import com.ecommerce.chatdemo.domain.wishlist.dto.WishListResponse;
import com.ecommerce.chatdemo.domain.wishlist.entity.WishList;
import com.ecommerce.chatdemo.domain.wishlist.exception.WishListErrorCode;
import com.ecommerce.chatdemo.domain.wishlist.exception.WishListException;
import com.ecommerce.chatdemo.domain.wishlist.repository.WishListRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.AuthException;
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
        // 생성일 기준 내림차순으로 조회
        Pageable pageable = PageRequest.of(page-1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        // 로그인한 사용자의 찜 목록만 조회
        Page<WishList> wishlist = wishListRepository.findByMemberId(memberId, pageable);

        return wishlist.map(WishListResponse::new);
    }

    // 찜 추가
    @Transactional
    public WishListResponse createWishlist(Long memberId, Long productId) {
        // 이미 추가한 상품인지 확인
        if (wishListRepository.existsByMemberIdAndProductId(memberId, productId)) {
            throw new WishListException(WishListErrorCode.WISHLIST_ALREADY_EXISTS);
        }

        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );

        Product product = productRepository.findById(productId).orElseThrow(
                () -> new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND)
        );

        WishList wishlist = WishList.builder()
                .member(member)
                .product(product)
                .build();
        wishListRepository.save(wishlist);

        return new WishListResponse(wishlist);
    }

    // 찜 삭제
    @Transactional
    public void deleteWishlist(Long productId) {
        // 해당 상품이 존재하는지 확인
        boolean existence = wishListRepository.existsByProductId(productId);
        if (!existence) {
            throw new ProductException(ProductErrorCode.PRODUCT_NOT_FOUND);
        }

        wishListRepository.deleteByProductId(productId);
    }
}
