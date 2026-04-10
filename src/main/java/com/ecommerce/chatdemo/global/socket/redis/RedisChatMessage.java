package com.ecommerce.chatdemo.global.socket.redis;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RedisChatMessage {
    private Long roomId;
    private Long senderId;
    private String senderName;
    private String content;
    private ChatMessageType type;
}
