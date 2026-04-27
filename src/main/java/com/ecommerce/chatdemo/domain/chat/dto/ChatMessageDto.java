package com.ecommerce.chatdemo.domain.chat.dto;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessageType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ChatMessageDto {
    private Long roomId;
    private Long userId;
    private String userName;
    private String content;
    private ChatMessageType type;
}
