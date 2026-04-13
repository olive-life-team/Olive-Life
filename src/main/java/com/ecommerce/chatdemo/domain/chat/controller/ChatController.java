package com.ecommerce.chatdemo.domain.chat.controller;

import com.ecommerce.chatdemo.domain.chat.dto.ChatMessageDto;
import com.ecommerce.chatdemo.domain.chat.dto.TypingIndicatorDto;
import com.ecommerce.chatdemo.domain.chat.entity.ChatMessage;
import com.ecommerce.chatdemo.domain.chat.entity.ChatMessageType;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.repository.ChatMessageRepository;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.global.socket.AuthenticatedUser;
import com.ecommerce.chatdemo.global.socket.redis.ChatRedisPublisher;
import com.ecommerce.chatdemo.global.socket.redis.RedisChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final ChatRedisPublisher chatRedisPublisher;

    // 메시지 입력시 저장 및 발송 + 입장시 메시지 전송
    @MessageMapping("/chat.send")
    public void send(ChatMessageDto dto, Principal principal, SimpMessageHeaderAccessor headerAccessor) {
        // 1. 사용자 정보 로드 (매번 DB 조회보다 Principal 캐싱 권장)
        Member sender = AuthenticatedUser.fromPrincipal(principal);

        /**
         * 프론트엔드에서 웹소켓 연결 및 구독(subscribe) 직후에 type을 ENTER로 설정한 JSON 메시지를 한 번 쏴줘야 합니다.
         *
         * 동작 흐름:
         * 1.  사용자가 채팅방 입장 클릭
         * 2.  JS에서 웹소켓 연결 및 /sub/chat/{roomId} 구독
         * 3.  [중요] JS에서 stomp.send("/pub/chat.send", {}, JSON.stringify({type: 'ENTER', roomId: 1, ...})) 호출
         * 4.  그제야 위 코드가 실행되며 "○○님이 입장했습니다"가 저장 및 발송됨
         */
        // 2. 입장 로직 처리
        if (ChatMessageType.ENTER.equals(dto.getType())) {
            // 세션에 정보 저장 (퇴장 시 사용)
            headerAccessor.getSessionAttributes().put("roomId", dto.getRoomId());
            headerAccessor.getSessionAttributes().put("userId", sender.getId());
            headerAccessor.getSessionAttributes().put("userName", sender.getName());

            dto.setMessage(sender.getName() + "님이 입장했습니다.");

            // 입장 메시지는 DB에 저장하지 않고 바로 Redis로 발행 후 메서드 종료
            publishToRedis(dto.getRoomId(), sender, dto.getMessage(), ChatMessageType.ENTER);
            return;
        }

        // 3. DB 저장 및 발행
        ChatRoom room = chatRoomRepository.findById(dto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        ChatMessage message = ChatMessage.builder()
                .room(room)
                .sender(sender)
                .message(dto.getMessage())
                .messageType(dto.getType())
                .build();
        chatMessageRepository.save(message);

        // 4. Redis 발행
        publishToRedis(room.getId(), sender, dto.getMessage(), ChatMessageType.TALK);
    }

    // 입력중 표시
    @MessageMapping("/chat.typing")
    public void typing(TypingIndicatorDto dto, Principal principal) {
        Member member = AuthenticatedUser.fromPrincipal(principal);

        dto.setUserId(member.getId());
        dto.setUserName(member.getName());

        // 타이핑 상태를 해당 채팅방의 다른 사용자들에게 브로드캐스트
        messagingTemplate.convertAndSend("/sub/chat/" + dto.getRoomId() + "/typing", dto);
    }

    // Redis 발행 로직 공통화
    private void publishToRedis(Long roomId, Member sender, String message, ChatMessageType type) {
        RedisChatMessage redisMessage = new RedisChatMessage(
                roomId,
                sender.getId(),
                sender.getName(),
                message,
                type
        );
        chatRedisPublisher.publish(roomId, redisMessage);
    }
}
