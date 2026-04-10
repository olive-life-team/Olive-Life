package com.ecommerce.chatdemo.domain.coupon.entity;

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
@Table(name = "coupon")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Coupon extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "discount_value", nullable = false, precision = 15, scale = 0)
    private BigDecimal discountValue;

    @Column(nullable = false)
    private Integer quantity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private CouponStatus status;

    @Column(name = "issue_start_at", nullable = false)
    private LocalDateTime issueStartAt;

    @Column(name = "issue_end_at", nullable = false)
    private LocalDateTime issueEndAt;

    @Column(name = "use_start_at", nullable = false)
    private LocalDateTime useStartAt;

    @Column(name = "use_end_at", nullable = false)
    private LocalDateTime useEndAt;

    @Builder
    private Coupon(
            String name,
            BigDecimal discountValue,
            Integer quantity,
            CouponStatus status,
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
        this.name = name;
        this.discountValue = discountValue;
        this.quantity = quantity;
        this.status = status;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.useStartAt = useStartAt;
        this.useEndAt = useEndAt;
    }

    public static Coupon create(
            String name,
            BigDecimal discountValue,
            Integer quantity,
            CouponStatus status,
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
        return Coupon.builder()
                .name(name)
                .discountValue(discountValue)
                .quantity(quantity)
                .status(status)
                .issueStartAt(issueStartAt)
                .issueEndAt(issueEndAt)
                .useStartAt(useStartAt)
                .useEndAt(useEndAt)
                .build();
    }
}