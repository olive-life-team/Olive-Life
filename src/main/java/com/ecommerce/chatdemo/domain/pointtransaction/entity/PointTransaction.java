package com.ecommerce.chatdemo.domain.pointtransaction.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
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
@Table(name = "point_transaction")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "order_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Order order;

    @Column(nullable = false, precision = 15, scale = 0)
    private BigDecimal points;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 100)
    private PointTransactionType transactionType;

    @Column(name = "expired_at")
    private LocalDateTime expiredAt;

    @Builder
    private PointTransaction(
            Member member,
            Order order,
            BigDecimal points,
            PointTransactionType transactionType,
            LocalDateTime expiredAt
    ) {
        this.member = member;
        this.order = order;
        this.points = points;
        this.transactionType = transactionType;
        this.expiredAt = expiredAt;
    }

    public static PointTransaction create(
            Member member,
            Order order,
            BigDecimal points,
            PointTransactionType transactionType,
            LocalDateTime expiredAt
    ) {
        return PointTransaction.builder()
                .member(member)
                .order(order)
                .points(points)
                .transactionType(transactionType)
                .expiredAt(expiredAt)
                .build();
    }
}
