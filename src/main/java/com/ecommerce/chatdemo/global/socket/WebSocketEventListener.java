package com.ecommerce.chatdemo.global.socket;

import com.ecommerce.chatdemo.domain.chat.entity.ChatMessageType;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoomStatus;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import com.ecommerce.chatdemo.global.socket.redis.ChatRedisPublisher;
import com.ecommerce.chatdemo.global.socket.redis.RedisChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@RequiredArgsConstructor
@Slf4j
public class WebSocketEventListener {
    private final ChatRedisPublisher chatRedisPublisher;
    private final ChatRoomRepository chatRoomRepository;

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        // 1. 세션 헤더에서 정보를 추출하기 위한 Accessor 생성
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());

        // 2. 입장(ENTER) 시점에 세션에 저장해두었던 정보를 꺼냄
        Long roomId = (Long) headerAccessor.getSessionAttributes().get("roomId");
        Long userId = (Long) headerAccessor.getSessionAttributes().get("userId");
        String userName = (String) headerAccessor.getSessionAttributes().get("userName");

        if (roomId != null && userName != null) {
            log.info("강제 종료 감지: [방 번호: {}] [사용자: {}]", roomId, userName);

            // 3. DB 상태 변경 (강제 종료 시에도 상담 완료 처리)
            // 비즈니스 로직에 따라 상태를 바꿀지, 유지할지 결정합니다.
            /*chatRoomRepository.findById(roomId).ifPresent(room -> {
                if (room.getStatus() != ChatRoomStatus.COMPLETED) {
                    room.updateStatus(ChatRoomStatus.COMPLETED);
                    chatRoomRepository.save(room);
                    log.info("방 번호 {} 상담 상태를 COMPLETED로 변경했습니다.", roomId);
                }
            });*/

            // 4. 퇴장 시스템 메시지 생성 및 Redis 발행
            // 이 메시지는 실시간으로 접속 중인 다른 사용자(상담원 등)에게 전달됩니다.
            RedisChatMessage leaveMessage = new RedisChatMessage(
                    roomId,
                    userId,
                    userName,
                    userName + "님의 연결이 끊겼습니다",
                    ChatMessageType.LEAVE
            );

            chatRedisPublisher.publish(roomId, leaveMessage);
        }
    }
}
