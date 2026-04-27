package com.ecommerce.chatdemo.domain.order.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.order.exception.OrderErrorCode;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import com.ecommerce.chatdemo.global.exception.BusinessException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;


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

    // 정적메서드 + 엔터티에서 null 체크함으로서 서비스에서 반복적인 로직 제거
    public static Order create(
            Member member,
            String orderNumber,
            Long totalAmount,
            Long usedPoints,
            Long usedCouponPrice
    ) {
        long safeUsedPoints = usedPoints != null ? usedPoints : 0L;
        long safeUsedCouponPrice = usedCouponPrice != null ? usedCouponPrice : 0L;

        // 엔터티에서 최종 결제금액을 계산 -> 믿을 수 있음
        long finalPayAmount = totalAmount - safeUsedPoints - safeUsedCouponPrice;

        if (finalPayAmount < 0) {
            throw new BusinessException(OrderErrorCode.INVALID_FINAL_AMOUNT);
        }
        return Order.builder()
                .member(member)
                .orderNumber(orderNumber)
                .totalAmount(totalAmount)
                .usedPoints(safeUsedPoints)
                .usedCouponPrice(safeUsedCouponPrice)
                .finalAmount(finalPayAmount)
                .status(OrderStatus.COMPLETED)
                .build();
    }

    // 주문번호 생성기
    public static String generateOrderNumber() {
        return "ORD-"
                + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
                + "-"
                + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // 주문 취소 메서드
    public void cancel() {
        this.status = OrderStatus.CANCELED;
    }

}
