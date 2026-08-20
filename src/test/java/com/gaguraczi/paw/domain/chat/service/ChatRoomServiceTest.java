package com.gaguraczi.paw.domain.chat.service;

import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomCreateRes;
import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import com.gaguraczi.paw.domain.chat.repository.ChatMessageRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomParticipantRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomRepository;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ChatRoomServiceTest {

    @Mock
    private ChatRoomRepository chatRoomRepository;
    @Mock
    private ChatRoomParticipantRepository chatRoomParticipantRepository;
    @Mock
    private ChatMessageRepository chatMessageRepository;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private ChatRoomCreateTxService chatRoomCreateTxService;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private ChatRoomService chatRoomService;

    @Test
    void unique_충돌이면_바깥에서_기존_방을_재조회한다() {
        UUID buyerUid = UUID.randomUUID();
        UUID sellerUid = UUID.randomUUID();
        User buyer = User.builder().uid(buyerUid).build();
        User seller = User.builder().uid(sellerUid).build();
        Community post = marketPost(seller);
        when(post.getPostId()).thenReturn(33L);
        ChatRoom existing = room(12L, 33L, seller, buyer);

        when(securityUtils.currentUser()).thenReturn(buyer);
        when(communityRepository.findById(33L)).thenReturn(Optional.of(post));
        when(chatRoomRepository.findByPostIdAndBuyer_Uid(33L, buyerUid))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        when(chatRoomCreateTxService.insert(33L, sellerUid, buyerUid))
                .thenThrow(new DataIntegrityViolationException("uk_chat_room_post_buyer"));

        ChatRoomCreateRes res = chatRoomService.createOrGet(33L);

        assertThat(res.roomId()).isEqualTo(12L);
        verify(chatRoomCreateTxService).insert(33L, sellerUid, buyerUid);
        verify(chatRoomRepository, times(2)).findByPostIdAndBuyer_Uid(33L, buyerUid);
    }

    @Test
    void 신규_생성_성공_시_헬퍼_결과를_반환한다() {
        UUID buyerUid = UUID.randomUUID();
        UUID sellerUid = UUID.randomUUID();
        User buyer = User.builder().uid(buyerUid).build();
        User seller = User.builder().uid(sellerUid).build();
        Community post = marketPost(seller);
        when(post.getPostId()).thenReturn(33L);
        ChatRoom saved = room(15L, 33L, seller, buyer);

        when(securityUtils.currentUser()).thenReturn(buyer);
        when(communityRepository.findById(33L)).thenReturn(Optional.of(post));
        when(chatRoomRepository.findByPostIdAndBuyer_Uid(33L, buyerUid)).thenReturn(Optional.empty());
        when(chatRoomCreateTxService.insert(33L, sellerUid, buyerUid)).thenReturn(saved);

        ChatRoomCreateRes res = chatRoomService.createOrGet(33L);

        assertThat(res.roomId()).isEqualTo(15L);
        verify(chatRoomCreateTxService).insert(33L, sellerUid, buyerUid);
        verify(chatRoomRepository, times(1)).findByPostIdAndBuyer_Uid(33L, buyerUid);
    }

    @Test
    void 기존_방이_있으면_헬퍼를_호출하지_않는다() {
        UUID buyerUid = UUID.randomUUID();
        User buyer = User.builder().uid(buyerUid).build();
        User seller = User.builder().uid(UUID.randomUUID()).build();
        Community post = marketPost(seller);
        ChatRoom existing = room(12L, 33L, seller, buyer);

        when(securityUtils.currentUser()).thenReturn(buyer);
        when(communityRepository.findById(33L)).thenReturn(Optional.of(post));
        when(chatRoomRepository.findByPostIdAndBuyer_Uid(33L, buyerUid)).thenReturn(Optional.of(existing));

        ChatRoomCreateRes res = chatRoomService.createOrGet(33L);

        assertThat(res.roomId()).isEqualTo(12L);
        verify(chatRoomCreateTxService, never()).insert(33L, seller.getUid(), buyerUid);
    }

    private static Community marketPost(User seller) {
        Community post = mock(Community.class);
        when(post.getPostType()).thenReturn(PostType.MARKET);
        when(post.getUser()).thenReturn(seller);
        return post;
    }

    private static ChatRoom room(Long roomId, Long postId, User seller, User buyer) {
        return ChatRoom.builder()
                .roomId(roomId)
                .postId(postId)
                .seller(seller)
                .buyer(buyer)
                .lastMessageAt(LocalDateTime.of(2026, 8, 20, 11, 0))
                .build();
    }
}
