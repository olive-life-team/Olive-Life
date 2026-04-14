package com.ecommerce.chatdemo.domain.chat.controller;

import com.ecommerce.chatdemo.domain.chat.dto.ChatMessageResponse;
import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.dto.CreateChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.service.ChatRoomService;
import com.ecommerce.chatdemo.global.response.ApiResponse;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/chat/rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;

    /**
     * 채팅방 생성
     * @param title
     * @param loginUserInfo
     * @return
     */
    @PostMapping
    public ResponseEntity<ApiResponse<CreateChatRoomResponse>> createRoom(
            @Valid @RequestParam String title,
            @LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success(chatRoomService.createRoom(title, loginUserInfo.id())));
    }

    /**
     * 채팅방 목록 조회
     * @param loginUserInfo
     * @return
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getRooms(
            @LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.ok(ApiResponse.success(
                chatRoomService.getRooms(loginUserInfo.id())));
    }

    /**
     * 메시지 내역 조회
     * @param roomId
     * @param cursor
     * @param size
     * @return
     */
    @GetMapping("/{roomId}/messages")
    public ResponseEntity<ApiResponse<Slice<ChatMessageResponse>>> getMessages(
            @PathVariable Long roomId,
            @RequestParam(required = false) Long cursor,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(ApiResponse.success(
                chatRoomService.getMessages(roomId, cursor, size)));
    }

    /**
     * 채팅방 퇴실
     * @param roomId
     * @param loginUserInfo
     * @return
     */
    @PatchMapping("/{roomId}/leave")
    public ResponseEntity<ApiResponse<ChatRoomResponse>> leaveChatRoom(
            @PathVariable Long roomId,
            @LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.ok(ApiResponse.success(chatRoomService.leaveChatRoom(roomId, loginUserInfo.id())));
    }
}
