package com.ecommerce.chatdemo.domain.chat.repository;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {
}
