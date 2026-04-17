package com.ecommerce.chatdemo.domain.coupon.entity;

import com.ecommerce.chatdemo.domain.coupon.exception.CouponErrorCode;
import com.ecommerce.chatdemo.domain.coupon.exception.CouponException;
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

    @Column(nullable = false)
    private Long discountAmount;

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

    // 낙관 락 테스트 시 주석 해제
//    @Version
//    private Long version;

    @Builder
    private Coupon(
            String name,
            Long discountAmount,
            Integer quantity,
            CouponStatus status,
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
        this.name = name;
        this.discountAmount = discountAmount;
        this.quantity = quantity;
        this.status = status;
        this.issueStartAt = issueStartAt;
        this.issueEndAt = issueEndAt;
        this.useStartAt = useStartAt;
        this.useEndAt = useEndAt;
    }

    public static Coupon create(
            String name,
            Long discountAmount,
            Integer quantity,
            CouponStatus status,
            LocalDateTime issueStartAt,
            LocalDateTime issueEndAt,
            LocalDateTime useStartAt,
            LocalDateTime useEndAt
    ) {
        // 발급일이 발급종료일보다 늦으면 예외
        if (issueStartAt.isAfter(issueEndAt)) {
            throw new CouponException(CouponErrorCode.INVALID_ISSUE_DATE);
        }
        // 사용가능 시작일이 만료일보다 늦으면 예외
        if (useStartAt.isAfter(useEndAt)) {
            throw new CouponException(CouponErrorCode.INVALID_USE_DATE);
        }
        return Coupon.builder()
                .name(name)
                .discountAmount(discountAmount)
                .quantity(quantity)
                .status(status)
                .issueStartAt(issueStartAt)
                .issueEndAt(issueEndAt)
                .useStartAt(useStartAt)
                .useEndAt(useEndAt)
                .build();
    }

    // TODO: 동시성 문제
    public void decreaseCouponQuantity() {
        if (this.quantity <= 0) {
            throw new CouponException(CouponErrorCode.COUPON_OUT_OF_STOCK);
        }
        this.quantity--;
    }
}