package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatMessage;
import com.gaguraczi.paw.domain.chat.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "채팅 메시지")
public record ChatMessageRes(
        Long messageId,
        UUID senderId,
        @Schema(description = "현재 로그인 유저가 보낸 메시지인지 여부") boolean mine,
        MessageType type,
        String content,
        String imageUrl,
        LocalDateTime sentAt
) {
    public static ChatMessageRes from(ChatMessage message, UUID viewerUid) {
        return new ChatMessageRes(
                message.getMessageId(),
                message.getSender().getUid(),
                message.getSender().getUid().equals(viewerUid),
                message.getMessageType(),
                message.getContent(),
                message.getImageUrl(),
                message.getCreatedAt()
        );
    }
}
