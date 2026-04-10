package com.ecommerce.chatdemo.domain.category.entity;

import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "category")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Category extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parentCategory;

    @Builder
    private Category(String name, Category parentCategory) {
        this.name = name;
        this.parentCategory = parentCategory;
    }

    public static Category of(String name, Category parentCategory) {
        return Category.builder()
                .name(name)
                .parentCategory(parentCategory)
                .build();
    }
}