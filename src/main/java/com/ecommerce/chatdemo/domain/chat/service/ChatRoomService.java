package com.ecommerce.chatdemo.domain.chat.service;

import com.ecommerce.chatdemo.domain.chat.dto.ChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.dto.CreateChatRoomResponse;
import com.ecommerce.chatdemo.domain.chat.entity.ChatRoom;
import com.ecommerce.chatdemo.domain.chat.repository.ChatRoomRepository;
import com.ecommerce.chatdemo.domain.member.entity.Member;
import com.ecommerce.chatdemo.domain.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    // 고객이 채팅방 생성
    @Transactional
    public CreateChatRoomResponse createRoom(String title, Long memberId) {
        Member member = memberRepository.findById(memberId).orElseThrow(
                () -> new IllegalArgumentException("없는 유저입니다.")
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
        List<ChatRoom> wishList = chatRoomRepository.findByMemberId(memberId);
        return wishList.stream()
                .map(ChatRoomResponse::new)
                .toList();
    }
}
