package com.ecommerce.chatdemo.domain.membercoupon.repository;

import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import com.ecommerce.chatdemo.domain.order.entity.Order;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

    Optional<MemberCoupon> findByOrder(Order order);

    boolean existsByMemberIdAndCouponId(Long memberId, Long couponId);

    @Query("SELECT mc FROM MemberCoupon mc " +
            "JOIN FETCH mc.coupon " +
            "WHERE mc.member.id = :memberId")
    Page<MemberCoupon> findByMemberIdWithPage(@Param("memberId") Long memberId, Pageable pageable);

    @Query("SELECT mc FROM MemberCoupon mc " +
            "JOIN FETCH mc.coupon " +
            "WHERE mc.member.id = :memberId")
    List<MemberCoupon> findByMemberId(@Param("memberId") Long memberId);
}
