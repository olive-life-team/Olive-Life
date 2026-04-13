package com.ecommerce.chatdemo.domain.chat.dto;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessage;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageResponse {
    private Long messageId;
    private String content;
    private Long senderId;
    private String senderName;
    private LocalDateTime createdAt;

    public ChatMessageResponse(ChatMessage message) {
        this.messageId = message.getId();
        this.content = message.getContent();
        this.senderId = message.getSender().getId();
        this.senderName = message.getSender().getName();
        this.createdAt = message.getCreatedAt();
    }
}
