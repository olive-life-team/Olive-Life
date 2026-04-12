package com.ecommerce.chatdemo.domain.order.controller;

import com.ecommerce.chatdemo.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
}
