package com.ecommerce.chatdemo.domain.chat.dto;

import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class ChatRoomResponse {
    private Long id;
    private String title;
    private ChatRoomStatus status;
    private Long memberId;
    private Long adminId;
    private LocalDateTime createdAt;

    public ChatRoomResponse(ChatRoom chatRoom) {
        this.id = chatRoom.getId();
        this.title = chatRoom.getTitle();
        this.status = chatRoom.getStatus();
        this.memberId = chatRoom.getMember().getId();
        this.adminId = (chatRoom.getAdmin() != null) ? chatRoom.getAdmin().getId() : null;
        this.createdAt = chatRoom.getCreatedAt();
    }
}
