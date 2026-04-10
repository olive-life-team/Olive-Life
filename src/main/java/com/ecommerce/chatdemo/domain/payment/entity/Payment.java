package com.ecommerce.chatdemo.domain.payment.entity;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Entity
@Table(name = "payment")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "order_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Order order;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal amount;

    @Column(name = "paid_at", nullable = false)
    private LocalDateTime paidAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    @Builder
    private Payment(Order order, BigDecimal amount, LocalDateTime paidAt, LocalDateTime refundedAt) {
        this.order = order;
        this.amount = amount;
        this.paidAt = paidAt;
        this.refundedAt = refundedAt;
    }

    public static Payment create(Order order, BigDecimal amount, LocalDateTime paidAt) {
        return Payment.builder()
                .order(order)
                .amount(amount)
                .paidAt(paidAt)
                .build();
    }
}