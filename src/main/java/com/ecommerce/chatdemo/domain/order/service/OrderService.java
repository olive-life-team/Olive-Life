package com.ecommerce.chatdemo.domain.order.service;

import com.ecommerce.chatdemo.domain.order.repository.OrderRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
}
