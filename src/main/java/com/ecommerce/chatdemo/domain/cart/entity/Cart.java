package com.ecommerce.chatdemo.domain.cart.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "cart")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Cart extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false, unique = true)
    @OneToOne(fetch = FetchType.LAZY)
    private Member member;

    @Builder
    private Cart(Member member) {
        this.member = member;
    }

    public static Cart create(Member member) {
        return Cart.builder()
                .member(member)
                .build();
    }
}
