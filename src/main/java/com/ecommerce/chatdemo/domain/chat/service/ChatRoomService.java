package com.ecommerce.chatdemo.domain.chat.service;

import com.ecommerce.chatdemo.domain.chat.dto.ChatMessageResponse;
import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.dto.CreateChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatMessage;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.repository.ChatMessageRepository;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import com.ecommerce.chatdemo.global.exception.AuthErrorCode;
import com.ecommerce.chatdemo.global.exception.AuthException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final MemberRepository memberRepository;

    // 고객이 채팅방 생성
    @Transactional
    public CreateChatRoomResponse createRoom(String title, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new AuthException(AuthErrorCode.USER_NOT_FOUND)
        );

        ChatRoom chatRoom = ChatRoom.builder()
               .title(title)
               .member(member)
               .build();
       chatRoomRepository.save(chatRoom);

       return new CreateChatRoomResponse(chatRoom);
    }

    // 본인(고객) 채팅방 목록 조회
    @Transactional(readOnly = true)
    public List<ChatRoomResponse> getRooms(Long memberId) {
        List<ChatRoom> chatRooms = chatRoomRepository.findByMemberId(memberId);
        return chatRooms.stream()
                .map(ChatRoomResponse::new)
                .toList();
    }

    // 메시지 내역 조회
    @Transactional(readOnly = true)
    public Slice<ChatMessageResponse> getMessages(Long roomId, Long cursor, int size) {
        Pageable pageable = PageRequest.of(0, size, Sort.by("id").descending());

        Slice<ChatMessage> messages = (cursor == null)
                ? chatMessageRepository.findFirstByChatRoomId(roomId, pageable)
                : chatMessageRepository.findByChatRoomIdAndIdLessThan(roomId, cursor, pageable);

        return messages.map(ChatMessageResponse::new);
    }
}
