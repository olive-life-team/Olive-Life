package com.ecommerce.chatdemo.domain.wishlist.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.product.entity.Product;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "wish_list",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_wish_list_member_product", columnNames = {"member_id", "product_id"})
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WishList extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "product_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Product product;

    @Builder
    private WishList(Member member, Product product) {
        this.member = member;
        this.product = product;
    }

    public static WishList create(Member member, Product product) {
        return WishList.builder()
                .member(member)
                .product(product)
                .build();
    }
}
