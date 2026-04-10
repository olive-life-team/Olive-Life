package com.ecommerce.chatdemo.domain.category.repository;

import com.ecommerce.chatdemo.domain.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}
