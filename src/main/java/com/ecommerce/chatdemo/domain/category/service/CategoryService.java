package com.ecommerce.chatdemo.domain.category.service;

import com.ecommerce.chatdemo.domain.category.dto.response.CategoryResponse;
import com.ecommerce.chatdemo.domain.category.entity.Category;
import com.ecommerce.chatdemo.domain.category.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryService {
    private final CategoryRepository repository;


    public List<CategoryResponse> getCategories() {
        List<Category> categoryList = repository.findByOrderByNameAsc();
        return categoryList.stream()
                .map(category -> CategoryResponse.from(category))
                .collect(Collectors.toList());
    }
}
