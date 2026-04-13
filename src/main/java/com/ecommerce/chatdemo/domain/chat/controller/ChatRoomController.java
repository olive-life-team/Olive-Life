package com.ecommerce.chatdemo.domain.chat.controller;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.dto.CreateChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.service.ChatRoomService;
import com.ecommerce.chatdemo.global.security.annotation.LoginUser;
import com.ecommerce.chatdemo.global.security.dto.LoginUserInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<CreateChatRoomResponse> createRoom(
            @RequestParam String title,
            @LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(chatRoomService.createRoom(title, loginUserInfo.id()));
    }

    /**
     * 채팅방 목록 조회
     * @param loginUserInfo
     * @return
     */
    @GetMapping
    public ResponseEntity<List<ChatRoomResponse>> getRooms(
            @LoginUser LoginUserInfo loginUserInfo) {
        return ResponseEntity.ok(chatRoomService.getRooms(loginUserInfo.id()));
    }

    // 메시지 내역 조회

    // 채팅방 퇴실

}
