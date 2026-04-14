package com.ecommerce.chatdemo.domain.chat.service;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import com.ecommerce.chatdemo.domain.chat.exception.ChatErrorCode;
import com.ecommerce.chatdemo.domain.chat.exception.ChatException;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 상태별 목록 조회 (관리자)
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(ChatRoomStatus status) {
        return chatRoomRepository.findAllByStatus(status)
                .stream()
                .map(ChatRoomResponse::new)
                .toList();
    }

    // 관리자가 채팅방 상태 변경
    @Transactional
    public ChatRoomResponse updateStatus(Long roomId, ChatRoomStatus status) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
                () -> new ChatException(ChatErrorCode.CHATROOM_NOT_FOUND)
        );
        chatRoom.updateStatus(status);

        return new ChatRoomResponse(chatRoom);
    }
}
