package com.ecommerce.chatdemo.domain.product.repository;

import com.ecommerce.chatdemo.domain.product.entity.request.ProductSearchRequest;
import com.ecommerce.chatdemo.domain.product.entity.response.ProductSummaryResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static com.ecommerce.chatdemo.domain.product.entity.QProduct.product;
import static com.querydsl.core.types.Projections.constructor;

@RequiredArgsConstructor
public class ProductRepositoryCustomImpl implements ProductRepositoryCustom {

    private final JPAQueryFactory queryFactory;


    /**
     // 전체 데이터
     SELECT *
     FROM product
     WHERE product.name LIKE "%request.keyword%"
     LIMIT pageRequest.getPageSize()
     OFFSET pageRequest.getOffset()

     // 전체갯수
     SELECT COUNT(*)
     FROM product
     WHERE product.name LIKE "%request.keyword%"
     */
    @Override
    public Page<ProductSummaryResponse> search(ProductSearchRequest request) {
        PageRequest pageRequest = PageRequest.of(request.page(), request.size());


        List<ProductSummaryResponse> responseList = queryFactory
                .select(Projections.constructor(
                        ProductSummaryResponse.class,
                        product.id,
                        product.name,
                        product.price,
                        product.status,
                        product.stock
                ))
                .from(product)
                .where(product.name.contains(request.keyword()))
                .limit(pageRequest.getPageSize())
                .offset(pageRequest.getOffset())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(product.name.contains(request.keyword()))
                .fetchOne();


        return new PageImpl<>(responseList, pageRequest, total);
    }
}
