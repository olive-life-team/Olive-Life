package com.ecommerce.chatdemo.domain.chat.service;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(ChatRoomStatus status) {
        return chatRoomRepository.findAllByStatus(status)
                .stream()
                .map(ChatRoomResponse::new)
                .toList();
    }
}
