package com.ecommerce.chatdemo.domain.membercoupon.entity;

import com.ecommerce.chatdemo.domain.coupon.entity.Coupon;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "member_coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberCoupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "coupon_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Coupon coupon;

    @JoinColumn(name = "order_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private MemberCouponStatus status;

    @Column(name = "issued_at")
    private LocalDateTime issuedAt;

    @Column(name = "used_at")
    private LocalDateTime usedAt;

    @Builder
    private MemberCoupon(
            Member member,
            Coupon coupon,
            Order order,
            MemberCouponStatus status,
            LocalDateTime issuedAt,
            LocalDateTime usedAt
    ) {
        this.member = member;
        this.coupon = coupon;
        this.order = order;
        this.status = status;
        this.issuedAt = issuedAt;
        this.usedAt = usedAt;
    }

    public static MemberCoupon issue(Member member, Coupon coupon) {
        return MemberCoupon.builder()
                .member(member)
                .coupon(coupon)
                .status(MemberCouponStatus.AVAILABLE)
                .issuedAt(LocalDateTime.now())
                .build();
    }
}