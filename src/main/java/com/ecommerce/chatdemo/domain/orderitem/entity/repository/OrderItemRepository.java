package com.ecommerce.chatdemo.domain.orderitem.entity.repository;

import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
}
