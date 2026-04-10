package com.ecommerce.chatdemo.domain.member.entity;

import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@Entity
@Table(
        name = "member",
        indexes = {
                @Index(name = "idx_member_email", columnList = "email"),
                @Index(name = "idx_member_name", columnList = "name")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "membership_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Membership membership;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private MemberRole role;

    @Column(name = "point_balance", nullable = false, precision = 15, scale = 0)
    private BigDecimal pointBalance;

    @Builder
    private Member(
            Membership membership,
            String email,
            String password,
            String name,
            MemberRole role,
            BigDecimal pointBalance
    ) {
        this.membership = membership;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.pointBalance = pointBalance == null ? BigDecimal.ZERO : pointBalance;
    }

    public static Member create(
            Membership membership,
            String email,
            String password,
            String name,
            MemberRole role
    ) {
        return Member.builder()
                .membership(membership)
                .email(email)
                .password(password)
                .name(name)
                .role(role == null ? MemberRole.CUSTOMER : role)
                .pointBalance(BigDecimal.ZERO)
                .build();
    }
}
