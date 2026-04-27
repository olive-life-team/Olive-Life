package com.ecommerce.chatdemo.domain.chat.controller;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import com.ecommerce.chatdemo.domain.chat.service.AdminChatRoomService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

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
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CS_ADMIN')")
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(
            @RequestParam(required = false) ChatRoomStatus status) {
        return ResponseEntity.ok(ApiResponse.success(adminChatRoomService.getRooms(status)));
    }

    /**
     * 채팅방 상태 변경
     * @param roomId
     * @param status
     * @return
     */
    @PreAuthorize("hasAnyRole('SUPER_ADMIN', 'CS_ADMIN')")
    @PatchMapping("/{roomId}/status")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> updateStatus(
            @PathVariable Long roomId,
            @RequestParam ChatRoomStatus status) {
        return ResponseEntity.ok(ApiResponse.success(adminChatRoomService.updateStatus(roomId, status)));
    }
}
