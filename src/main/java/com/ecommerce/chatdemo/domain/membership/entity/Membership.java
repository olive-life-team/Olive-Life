package com.ecommerce.chatdemo.domain.membership.entity;

import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(name = "membership")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Membership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "min_spent_amount", nullable = false, precision = 15, scale = 0)
    private Long minSpentAmount;

    @Column(name = "point_rate", nullable = false, precision = 5, scale = 2)
    private Long pointRate;

    @Builder
    private Membership(String name, Long minSpentAmount, Long pointRate) {
        this.name = name;
        this.minSpentAmount = minSpentAmount;
        this.pointRate = pointRate;
    }

    public static Membership create(String name, Long minSpentAmount, Long pointRate) {
        return Membership.builder()
                .name(name)
                .minSpentAmount(minSpentAmount)
                .pointRate(pointRate)
                .build();
    }
}