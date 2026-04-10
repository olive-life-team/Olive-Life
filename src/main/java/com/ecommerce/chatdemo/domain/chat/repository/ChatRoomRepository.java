package com.ecommerce.chatdemo.domain.chat.repository;

import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
}
