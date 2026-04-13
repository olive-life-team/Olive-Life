package com.ecommerce.chatdemo.domain.chat.repository;

import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByMemberId(Long memberId);
}
