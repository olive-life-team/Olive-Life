package com.ecommerce.chatdemo.domain.orderitem.entity.repository;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByOrder(Order order);

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.product " +
            " WHERE oi.order IN :orders")
    List<OrderItem> findByOrderIn(@Param("orders") List<Order> orders);

    @Query("SELECT oi FROM OrderItem oi " +
            "JOIN FETCH oi.product " +
            " WHERE oi.order = :order")
    List<OrderItem> findByOrderWithProduct(@Param("order") Order order);
}
