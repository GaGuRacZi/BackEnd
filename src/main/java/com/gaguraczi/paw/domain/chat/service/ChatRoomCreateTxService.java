package com.gaguraczi.paw.domain.chat.service;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import com.gaguraczi.paw.domain.chat.entity.ChatRoomParticipant;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomParticipantRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ChatRoomCreateTxService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final UserRepository userRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public ChatRoom insert(Long postId, UUID sellerUid, UUID buyerUid) {
        User seller = userRepository.getReferenceById(sellerUid);
        User buyer = userRepository.getReferenceById(buyerUid);
        ChatRoom saved = chatRoomRepository.saveAndFlush(ChatRoom.builder()
                .postId(postId)
                .seller(seller)
                .buyer(buyer)
                .lastMessageAt(LocalDateTime.now())
                .build());
        // 안읽음 카운트 서브쿼리가 항상 참여자 행을 찾을 수 있도록 생성 시점에 양쪽 참여자 행을 만들어 둔다.
        chatRoomParticipantRepository.save(ChatRoomParticipant.builder().room(saved).user(buyer).build());
        chatRoomParticipantRepository.save(ChatRoomParticipant.builder().room(saved).user(seller).build());
        return saved;
    }
}
