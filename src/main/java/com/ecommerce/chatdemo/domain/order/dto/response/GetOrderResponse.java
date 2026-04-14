package com.ecommerce.chatdemo.domain.order.dto.response;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.order.entity.OrderStatus;
import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class GetOrderResponse {

    private final Long orderId;
    private final String orderNumber;
    private final Long totalAmount;
    private final Long discountPrice;
    private final Long finalAmount;
    private final OrderStatus status;
    private final String orderSummary;
    private final LocalDateTime createdAt;

    public GetOrderResponse(Order order, List<OrderItem> orderItems) {
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.totalAmount = order.getTotalAmount();
        this.discountPrice = order.getUsedPoints() + order.getUsedCouponPrice();
        this.finalAmount = order.getFinalAmount();
        this.status = order.getStatus();
        this.orderSummary = buildSummary(orderItems);
        this.createdAt = order.getCreatedAt();
    }

    private String buildSummary(List<OrderItem> orderItems) {
        if (orderItems.isEmpty()) return "주문";
        String firstName = orderItems.get(0).getProductName();
        int count = orderItems.size();
        return count == 1 ? firstName : firstName + " 외 " + (count - 1) + "건";
    }
}
