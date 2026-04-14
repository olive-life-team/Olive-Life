package com.ecommerce.chatdemo.domain.order.dto.response;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.order.entity.OrderStatus;
import com.ecommerce.chatdemo.domain.orderitem.entity.OrderItem;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class OrderResponse {

    private final Long memberId;
    private final Long orderId;
    private final String orderNumber;
    private final Long totalAmount;
    private final Long discountPrice;
    private final Long finalAmount;
    private final OrderStatus status;
    private final List<OrderItemDetail> orderItemDetails;
    private final LocalDateTime createdAt;

    @Getter
    public static class OrderItemDetail {
        private final Long productId;
        private final String productName;
        private final Long orderPrice;
        private final Integer quantity;

        public OrderItemDetail(OrderItem orderItem) {
            this.productId = orderItem.getProduct().getId();
            this.productName = orderItem.getProductName();
            this.orderPrice = orderItem.getProductPrice();
            this.quantity = orderItem.getQuantity();
        }
    }

    public OrderResponse(Order order, List<OrderItem> orderItems) {
        this.memberId = order.getMember().getId();
        this.orderId = order.getId();
        this.orderNumber = order.getOrderNumber();
        this.totalAmount = order.getTotalAmount();
        this.discountPrice = order.getUsedPoints() + order.getUsedCouponPrice();
        this.finalAmount = order.getFinalAmount();
        this.status = order.getStatus();
        this.createdAt = order.getCreatedAt();
        this.orderItemDetails = orderItems.stream().map(OrderItemDetail::new).toList();
    }
}
