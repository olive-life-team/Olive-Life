package com.ecommerce.chatdemo.domain.chat.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(name = "chat_message")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JoinColumn(name = "room_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private ChatRoom room;

    @JoinColumn(name = "sender_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member sender;

    @Column(nullable = false, length = 255)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false, length = 100)
    private ChatMessageType messageType;

    @Builder
    private ChatMessage(
            ChatRoom room,
            Member sender,
            String message,
            ChatMessageType messageType

    ) {
        this.room = room;
        this.sender = sender;
        this.message = message;
        this.messageType = messageType;
    }

    /*public static ChatMessage create(
            ChatRoom room,
            Member sender,
            String message,
            ChatMessageType messageType
    ) {
        return ChatMessage.builder()
                .room(room)
                .sender(sender)
                .message(message)
                .messageType(messageType)
                .build();
    }*/
}
