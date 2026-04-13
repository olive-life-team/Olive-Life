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
@Table(name = "chat_room")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @JoinColumn(name = "member_id", nullable = false)
    @ManyToOne(fetch = FetchType.LAZY)
    private Member member;

    @JoinColumn(name = "admin_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Member admin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 100)
    private ChatRoomStatus status;

    @Builder
    private ChatRoom(String title, Member member) {
        this.title = title;
        this.member = member;
        this.status = ChatRoomStatus.WAITING;
    }

    /*public static ChatRoom create(Member member) {
        return ChatRoom.builder()
                .member(member)
                .status(ChatRoomStatus.WAITING)
                .build();
    }*/
}
