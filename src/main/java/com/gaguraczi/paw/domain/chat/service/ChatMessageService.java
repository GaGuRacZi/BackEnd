package com.gaguraczi.paw.domain.chat.service;

import com.gaguraczi.paw.domain.chat.dto.res.ChatMessageRes;
import com.gaguraczi.paw.domain.chat.entity.ChatMessage;
import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import com.gaguraczi.paw.domain.chat.enums.MessageType;
import com.gaguraczi.paw.domain.chat.exception.code.ChatErrorCode;
import com.gaguraczi.paw.domain.chat.fcm.ChatFcmService;
import com.gaguraczi.paw.domain.chat.repository.ChatMessageRepository;
import com.gaguraczi.paw.domain.chat.repository.ChatRoomRepository;
import com.gaguraczi.paw.domain.chat.support.ChatMessageCursorCodec;
import com.gaguraczi.paw.domain.community.support.CommunityImageValidator;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int DEFAULT_SIZE = 30;
    private static final int MAX_SIZE = 50;
    private static final int PREVIEW_MAX_LEN = 100;
    private static final String IMAGE_PREVIEW = "사진을 보냈습니다";

    private final ChatRoomRepository chatRoomRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final ChatFcmService chatFcmService;
    private final S3Utils s3Utils;
    private final SecurityUtils securityUtils;

    @Transactional
    public ChatMessageRes send(Long roomId, MessageType type, String content, MultipartFile image) {
        User me = securityUtils.currentUser();
        ChatRoom room = chatRoomRepository.findWithParticipantsForUpdate(roomId)
                .orElseThrow(() -> GeneralException.of(ChatErrorCode.ROOM_NOT_FOUND_404));
        ChatRoomService.assertParticipant(room, me.getUid());

        ChatMessage message = type == MessageType.IMAGE
                ? buildImageMessage(room, me, image)
                : buildTextMessage(room, me, content);
        chatMessageRepository.save(message);

        String preview = type == MessageType.IMAGE ? IMAGE_PREVIEW : truncate(content);
        room.recordLastMessage(preview, message.getCreatedAt());

        User opponent = room.opponentOf(me.getUid());
        String title = messageTitle(me.getNickname());
        Long roomIdValue = room.getRoomId();
        Long postId = room.getPostId();
        Long messageId = message.getMessageId();
        afterCommit(() -> chatFcmService.notifyMessage(
                opponent,
                me.getUid(),
                title,
                preview,
                roomIdValue,
                postId,
                messageId
        ));

        return ChatMessageRes.from(message, me.getUid());
    }

    @Transactional(readOnly = true)
    public CursorPageRes<ChatMessageRes> list(Long roomId, String cursor, Integer size) {
        User me = securityUtils.currentUser();
        ChatRoom room = chatRoomRepository.findWithParticipantsByRoomId(roomId)
                .orElseThrow(() -> GeneralException.of(ChatErrorCode.ROOM_NOT_FOUND_404));
        ChatRoomService.assertParticipant(room, me.getUid());

        int pageSize = normalizeSize(size);
        Long decodedCursor = ChatMessageCursorCodec.decode(cursor);
        List<ChatMessage> rows = chatMessageRepository.findPageByRoom(roomId, decodedCursor, PageRequest.of(0, pageSize + 1));

        boolean hasNext = rows.size() > pageSize;
        List<ChatMessage> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<ChatMessageRes> content = page.stream().map(m -> ChatMessageRes.from(m, me.getUid())).toList();

        String nextCursor = hasNext && !page.isEmpty()
                ? ChatMessageCursorCodec.encode(page.getLast().getMessageId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    private ChatMessage buildTextMessage(ChatRoom room, User sender, String content) {
        if (content == null || content.isBlank()) {
            throw GeneralException.of(ChatErrorCode.MESSAGE_CONTENT_REQUIRED_400);
        }
        return ChatMessage.builder()
                .room(room)
                .sender(sender)
                .messageType(MessageType.TEXT)
                .content(content)
                .build();
    }

    private ChatMessage buildImageMessage(ChatRoom room, User sender, MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw GeneralException.of(ChatErrorCode.MESSAGE_IMAGE_REQUIRED_400);
        }
        CommunityImageValidator.validate(image);
        S3Dto uploaded = s3Utils.uploadMultipartUnderDirectory(image, "chat");
        scheduleUploadedCleanupOnRollback(uploaded.getKey());
        return ChatMessage.builder()
                .room(room)
                .sender(sender)
                .messageType(MessageType.IMAGE)
                .imageUrl(uploaded.getUrl())
                .imageS3Key(uploaded.getKey())
                .build();
    }

    private static String messageTitle(String nickname) {
        if (nickname == null || nickname.isBlank()) {
            return "새 메시지";
        }
        return nickname + "님의 메시지";
    }

    private String truncate(String content) {
        if (content == null) {
            return null;
        }
        return content.length() > PREVIEW_MAX_LEN ? content.substring(0, PREVIEW_MAX_LEN) : content;
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }

    private void scheduleUploadedCleanupOnRollback(String key) {
        if (key == null || !TransactionSynchronizationManager.isSynchronizationActive()) {
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    s3Utils.deleteQuietly(key);
                }
            }
        });
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
