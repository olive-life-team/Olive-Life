package com.ecommerce.chatdemo.domain.chat.repository;

import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByMemberId(Long memberId);

    List<ChatRoom> findAllByStatus(ChatRoomStatus status);
}
