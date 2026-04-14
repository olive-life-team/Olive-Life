package com.ecommerce.chatdemo.domain.chat.controller;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import com.ecommerce.chatdemo.domain.chat.service.AdminChatRoomService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/chat/rooms")
@RequiredArgsConstructor
public class AdminChatRoomController {

    private final AdminChatRoomService adminChatRoomService;

    /**
     * 채팅방 목록 조회
     * @param status
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(
            @RequestParam(required = false) ChatRoomStatus status) {
        return ResponseEntity.ok(ApiResponse.success(adminChatRoomService.getRooms(status)));
    }

    // 채팅방 상태 변경

    // 채탕방 관리자 배정
}
