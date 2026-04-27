package com.ecommerce.chatdemo.domain.order.dto.response;

import com.ecommerce.chatdemo.domain.order.entity.Order;
import com.ecommerce.chatdemo.domain.order.entity.OrderStatus;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class CancelOrderResponse {

    private final Long orderId;
    private final Long refundAmount;
    private final String message;
    private final OrderStatus status;
    private final LocalDateTime modifiedAt;

    public CancelOrderResponse(Order order) {
        this.orderId = order.getId();
        this.refundAmount = order.getFinalAmount();
        this.message = "주문이 취소되었습니다. (환불 완료)";
        this.status = order.getStatus();
        this.modifiedAt = order.getModifiedAt();
    }
}
