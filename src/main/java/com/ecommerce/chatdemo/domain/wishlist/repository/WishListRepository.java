package com.ecommerce.chatdemo.domain.wishlist.repository;

import com.ecommerce.chatdemo.domain.wishlist.entity.WishList;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WishListRepository extends JpaRepository<WishList, Long> {

    Page<WishList> findByMemberId(Long memberId, Pageable pageable);
}
