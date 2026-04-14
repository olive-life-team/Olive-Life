package com.ecommerce.chatdemo.domain.order.repository;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    List<Order> findByMemberId(Long memberId);

    @Query("SELECT o FROM Order o " +
            "JOIN FETCH o.member " +
            "WHERE o.id = :orderId")
    Optional<Order> findByIdWithMember(@Param("orderId") Long orderId);
}
