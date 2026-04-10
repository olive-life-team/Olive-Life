package com.ecommerce.chatdemo.domain.refund.entity;

import com.ecommerce.chatdemo.domain.payment.entity.Payment;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "refund")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Refund extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "payment_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Payment payment;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal price;

    @Column(length = 255)
    private String reason;

    @Builder
    private Refund(Payment payment, BigDecimal price, String reason) {
        this.payment = payment;
        this.price = price;
        this.reason = reason;
    }

    public static Refund create(Payment payment, BigDecimal price, String reason) {
        return Refund.builder()
                .payment(payment)
                .price(price)
                .reason(reason)
                .build();
    }
}
