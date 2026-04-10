package com.ecommerce.chatdemo.domain.order.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

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

    @Column(name = "total_amount", nullable = false, precision = 15, scale = 0)
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private OrderStatus status;

    @Column(name = "used_points", precision = 15, scale = 0)
    private BigDecimal usedPoints;

    @Column(name = "used_coupon_price", precision = 15, scale = 0)
    private BigDecimal usedCouponPrice;

    @Builder
    private Order(
            Member member,
            String orderNumber,
            BigDecimal totalAmount,
            OrderStatus status,
            BigDecimal usedPoints,
            BigDecimal usedCouponPrice
    ) {
        this.member = member;
        this.orderNumber = orderNumber;
        this.totalAmount = totalAmount;
        this.status = status;
        this.usedPoints = usedPoints == null ? BigDecimal.ZERO : usedPoints;
        this.usedCouponPrice = usedCouponPrice == null ? BigDecimal.ZERO : usedCouponPrice;
    }

    public static Order create(
            Member member,
            String orderNumber,
            BigDecimal totalAmount,
            BigDecimal usedPoints,
            BigDecimal usedCouponPrice
    ) {
        return Order.builder()
                .member(member)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .status(OrderStatus.COMPLETED)
                .usedPoints(usedPoints)
                .usedCouponPrice(usedCouponPrice)
                .build();
    }

}
