package com.ecommerce.chatdemo.domain.chat.entity;

public enum ChatRoomStatus {
    WAITING,     // 대기중
    IN_PROGRESS, // 처리중
    COMPLETED;   // 완료

    public boolean canTransitionTo(ChatRoomStatus nextStatus) {
        return switch (this) {
            case WAITING -> nextStatus == IN_PROGRESS || nextStatus == COMPLETED;
            case IN_PROGRESS -> nextStatus == COMPLETED;
            case COMPLETED -> false;
        };
    }
}
