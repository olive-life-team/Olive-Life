package com.ecommerce.chatdemo.domain.member.entity;

import com.ecommerce.chatdemo.domain.membership.entity.Membership;
import com.ecommerce.chatdemo.domain.pointtransaction.entity.exception.PointErrorCode;
import com.ecommerce.chatdemo.domain.pointtransaction.entity.exception.PointException;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private MemberRole role;

    @Column(name = "point_balance", nullable = false)
    private Long pointBalance;

    @Builder
    private Member(
            Membership membership,
            String email,
            String password,
            String name,
            MemberRole role,
            Long pointBalance
    ) {
        this.membership = membership;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.pointBalance = pointBalance != null ?  pointBalance : 0L;
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
                .pointBalance(0L)
                .build();
    }

    // 포인트 검증
    public void validatePoint(Long usePoints) {
        if (usePoints < 0) {
            throw new PointException(PointErrorCode.INVALID_POINT);
        }
        if (this.pointBalance < usePoints) {
            throw new PointException(PointErrorCode.INSUFFICIENT_POINTS);
        }
    }

    // 포인트 차감 메서드
    public void decreasePointBalance(Long usePoints) {
        this.pointBalance -= usePoints;
    }
}
