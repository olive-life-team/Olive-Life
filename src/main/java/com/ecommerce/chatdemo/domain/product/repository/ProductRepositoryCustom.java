package com.ecommerce.chatdemo.domain.product.repository;

import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import org.springframework.data.domain.Page;

public interface ProductRepositoryCustom {
    Page<ProductSummaryResponse> search(ProductSearchRequest request);
}
