package com.ecommerce.chatdemo.domain.chat.entity;

import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.entity.BaseEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
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

    @NotBlank(message = "채팅방 이름은 필수이며 공백으로만 구성될 수 없습니다.")
    @Size(min = 2, max = 20, message = "채팅방 이름은 2자 이상 20자 이하로 입력해주세요.")
    @Pattern(
            regexp = "^[a-zA-Z0-9가-힣\\s]*$",
            message = "채팅방 이름은 한글, 영문, 숫자, 공백만 허용되며 특수문자는 사용할 수 없습니다."
    )
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
