package com.ecommerce.chatdemo.domain.order.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Getter
@Entity
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @Column(name = "order_number", nullable = false, unique = true, length = 100)
    private String orderNumber;

    @Column(name = "total_amount", nullable = false)
    private Long totalAmount; // 주문 총 금액(할인, 쿠폰 적용X)

    @Column(name = "used_points")
    private Long usedPoints;

    @Column(name = "used_coupon_price")
    private Long usedCouponPrice;

    @Column(name = "final_amount",nullable = false)
    private Long finalAmount; // 최종 결제 금액(totalAmount - usedPoints - usedCouponPrice)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private OrderStatus status;


    @Builder
    private Order(
            Member member,
            String orderNumber,
            Long totalAmount,
            Long usedPoints,
            Long usedCouponPrice,
            Long finalAmount,
            OrderStatus status
            ) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.usedPoints = usedPoints != null ? usedPoints : 0L;
        this.usedCouponPrice = usedCouponPrice != null ? usedCouponPrice : 0L;
        this.finalAmount = finalAmount;
        this.status = status;
    }

    public static Order create(
            Member member,
            String orderNumber,
            Long totalAmount,
            Long usedPoints,
            Long usedCouponPrice,
            Long finalAmount
    ) {
        return Order.builder()
                .member(member)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .usedPoints(usedPoints)
                .usedCouponPrice(usedCouponPrice)
                .finalAmount(finalAmount)
                .status(OrderStatus.COMPLETED)
                .build();
    }

}
