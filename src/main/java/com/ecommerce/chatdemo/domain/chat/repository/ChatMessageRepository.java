package com.ecommerce.chatdemo.domain.chat.repository;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // 처음 조회할 때 (cursor가 null일 때)
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId ORDER BY m.id DESC")
    Slice<ChatMessage> findFirstByChatRoomId(@Param("roomId") Long roomId, Pageable pageable);

    // 커서(마지막으로 본 ID) 이후의 데이터를 조회할 때
    @Query("SELECT m FROM ChatMessage m WHERE m.room.id = :roomId AND m.id < :cursor ORDER BY m.id DESC")
    Slice<ChatMessage> findByChatRoomIdAndIdLessThan(@Param("roomId") Long roomId, @Param("cursor") Long cursor, Pageable pageable);
}
