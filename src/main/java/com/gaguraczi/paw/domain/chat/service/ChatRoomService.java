package com.gaguraczi.paw.domain.chat.service;

import com.gaguraczi.paw.domain.chat.dto.res.ChatPostSummaryRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomCreateRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomDetailRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatRoomListItemRes;
import com.gaguraczi.paw.domain.chat.dto.res.ChatUserSummaryRes;
import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import com.gaguraczi.paw.domain.chat.entity.ChatRoomParticipant;
import com.gaguraczi.paw.domain.chat.exception.code.ChatErrorCode;
import com.gaguraczi.paw.domain.chat.repository.ChatMessageRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomParticipantRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomRepository;
import com.gaguraczi.paw.domain.chat.support.ChatRoomCursorCodec;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomParticipantRepository chatRoomParticipantRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final CommunityRepository communityRepository;
    private final SecurityUtils securityUtils;

    @Transactional
    public ChatRoomCreateRes createOrGet(Long postId) {
        User buyer = securityUtils.currentUser();
        Community post = communityRepository.findById(postId)
                .orElseThrow(() -> GeneralException.of(ChatErrorCode.POST_NOT_FOUND_404));
        if (post.getPostType() != PostType.MARKET) {
            throw GeneralException.of(ChatErrorCode.POST_TYPE_UNSUPPORTED_400);
        }
        if (post.getUser().getUid().equals(buyer.getUid())) {
            throw GeneralException.of(ChatErrorCode.SELF_CHAT_FORBIDDEN_403);
        }

        return chatRoomRepository.findByPostIdAndBuyer_Uid(postId, buyer.getUid())
                .map(ChatRoomCreateRes::from)
                .orElseGet(() -> ChatRoomCreateRes.from(createRoom(post, buyer)));
    }

    private ChatRoom createRoom(Community post, User buyer) {
        ChatRoom room = ChatRoom.builder()
                .postId(post.getPostId())
                .seller(post.getUser())
                .buyer(buyer)
                .lastMessageAt(LocalDateTime.now())
                .build();
        try {
            ChatRoom saved = chatRoomRepository.saveAndFlush(room);
            // 안읽음 카운트 서브쿼리가 항상 참여자 행을 찾을 수 있도록 생성 시점에 양쪽 참여자 행을 만들어 둔다.
            chatRoomParticipantRepository.save(ChatRoomParticipant.builder().room(saved).user(buyer).build());
            chatRoomParticipantRepository.save(ChatRoomParticipant.builder().room(saved).user(post.getUser()).build());
            return saved;
        } catch (DataIntegrityViolationException e) {
            // 동시 클릭으로 인한 unique 제약 충돌 시 기존 방을 재조회해서 idempotent하게 반환
            return chatRoomRepository.findByPostIdAndBuyer_Uid(post.getPostId(), buyer.getUid())
                    .orElseThrow(() -> e);
        }
    }

    @Transactional(readOnly = true)
    public ChatRoomDetailRes getDetail(Long roomId) {
        User me = securityUtils.currentUser();
        ChatRoom room = chatRoomRepository.findWithParticipantsByRoomId(roomId)
                .orElseThrow(() -> GeneralException.of(ChatErrorCode.ROOM_NOT_FOUND_404));
        assertParticipant(room, me.getUid());

        ChatUserSummaryRes opponent = ChatUserSummaryRes.from(room.opponentOf(me.getUid()));
        ChatPostSummaryRes post = resolvePostSummary(room.getPostId());
        return ChatRoomDetailRes.of(room, opponent, post);
    }

    @Transactional(readOnly = true)
    public CursorPageRes<ChatRoomListItemRes> list(String cursor, Integer size) {
        User me = securityUtils.currentUser();
        int pageSize = normalizeSize(size);
        ChatRoomCursorCodec.Cursor decoded = ChatRoomCursorCodec.decode(cursor);

        List<ChatRoom> rows = chatRoomRepository.findRoomsByUser(
                me.getUid(),
                decoded == null ? null : decoded.lastMessageAt(),
                decoded == null ? null : decoded.roomId(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<ChatRoom> page = hasNext ? rows.subList(0, pageSize) : rows;

        Map<Long, Long> unreadCounts = countUnread(page, me.getUid());
        Map<Long, ChatPostSummaryRes> postSummaries = resolvePostSummaries(page);

        List<ChatRoomListItemRes> content = new ArrayList<>(page.size());
        for (ChatRoom room : page) {
            ChatUserSummaryRes opponent = ChatUserSummaryRes.from(room.opponentOf(me.getUid()));
            ChatPostSummaryRes post = postSummaries.get(room.getPostId());
            content.add(ChatRoomListItemRes.of(room, opponent, post, unreadCounts.getOrDefault(room.getRoomId(), 0L)));
        }

        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            ChatRoom last = page.getLast();
            nextCursor = ChatRoomCursorCodec.encode(last.getLastMessageAt(), last.getRoomId());
        }
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    @Transactional
    public void markRead(Long roomId, Long lastReadMessageId) {
        User me = securityUtils.currentUser();
        ChatRoom room = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> GeneralException.of(ChatErrorCode.ROOM_NOT_FOUND_404));
        assertParticipant(room, me.getUid());

        if (lastReadMessageId != null && !chatMessageRepository.existsByMessageIdAndRoom_RoomId(lastReadMessageId, roomId)) {
            throw GeneralException.of(ChatErrorCode.MESSAGE_NOT_FOUND_404);
        }

        ChatRoomParticipant participant = chatRoomParticipantRepository
                .findByRoom_RoomIdAndUser_Uid(roomId, me.getUid())
                .orElseGet(() -> chatRoomParticipantRepository.save(
                        ChatRoomParticipant.builder().room(room).user(me).build()
                ));
        participant.markRead(lastReadMessageId);
    }

    private ChatPostSummaryRes resolvePostSummary(Long postId) {
        return communityRepository.findById(postId)
                .map(ChatPostSummaryRes::from)
                .orElseGet(() -> ChatPostSummaryRes.deleted(postId));
    }

    private Map<Long, ChatPostSummaryRes> resolvePostSummaries(List<ChatRoom> rooms) {
        List<Long> postIds = rooms.stream().map(ChatRoom::getPostId).distinct().toList();
        if (postIds.isEmpty()) {
            return Map.of();
        }
        Map<Long, ChatPostSummaryRes> found = communityRepository.findByPostIdIn(postIds).stream()
                .collect(Collectors.toMap(Community::getPostId, ChatPostSummaryRes::from));
        Map<Long, ChatPostSummaryRes> result = new HashMap<>();
        for (Long postId : postIds) {
            result.put(postId, found.getOrDefault(postId, ChatPostSummaryRes.deleted(postId)));
        }
        return result;
    }

    private Map<Long, Long> countUnread(List<ChatRoom> rooms, UUID uid) {
        if (rooms.isEmpty()) {
            return Map.of();
        }
        List<Long> roomIds = rooms.stream().map(ChatRoom::getRoomId).toList();
        return chatMessageRepository.countUnreadByRooms(roomIds, uid).stream()
                .collect(Collectors.toMap(ChatMessageRepository.UnreadCount::getRoomId, ChatMessageRepository.UnreadCount::getCnt));
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    static void assertParticipant(ChatRoom room, UUID uid) {
        if (!room.hasParticipant(uid)) {
            throw GeneralException.of(ChatErrorCode.NOT_PARTICIPANT_403);
        }
    }
}
