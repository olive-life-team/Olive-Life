package com.ecommerce.chatdemo.domain.membercoupon.repository;

import com.ecommerce.chatdemo.domain.membercoupon.entity.MemberCoupon;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberCouponRepository extends JpaRepository<MemberCoupon, Long> {

}
