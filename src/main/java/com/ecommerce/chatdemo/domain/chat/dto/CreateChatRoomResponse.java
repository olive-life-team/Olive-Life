package com.ecommerce.chatdemo.domain.chat.dto;

import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CreateChatRoomResponse {
    private Long id;
    private String title;

    public CreateChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
    }
}
