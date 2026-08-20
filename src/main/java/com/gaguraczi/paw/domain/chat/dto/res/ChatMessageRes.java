package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatMessage;
import com.gaguraczi.paw.domain.chat.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "채팅 메시지. TEXT는 content, IMAGE는 imageUrl을 씁니다.")
public record ChatMessageRes(
        @Schema(description = "메시지 ID", example = "501")
        Long messageId,
        @Schema(description = "보낸 사람 uid", example = "22222222-2222-2222-2222-222222222222")
        UUID senderId,
        @Schema(description = "현재 로그인 유저가 보낸 메시지인지. 말풍선 방향에 사용", example = "true")
        boolean mine,
        @Schema(description = "TEXT | IMAGE", example = "TEXT")
        MessageType type,
        @Schema(description = "TEXT 본문. IMAGE면 null", example = "네 가능해요")
        String content,
        @Schema(description = "IMAGE URL. TEXT면 null", example = "https://cdn.example.com/chat/502.jpg")
        String imageUrl,
        @Schema(description = "전송 시각", example = "2026-08-20T11:31:00")
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
