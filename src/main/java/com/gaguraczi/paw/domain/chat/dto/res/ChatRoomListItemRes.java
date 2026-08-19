package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 목록 아이템")
public record ChatRoomListItemRes(
        Long roomId,
        ChatUserSummaryRes opponent,
        ChatPostSummaryRes post,
        @Schema(example = "네 가능해요") String lastMessagePreview,
        LocalDateTime lastMessageAt,
        @Schema(example = "3") long unreadCount
) {
    public static ChatRoomListItemRes of(
            ChatRoom room,
            ChatUserSummaryRes opponent,
            ChatPostSummaryRes post,
            long unreadCount
    ) {
        return new ChatRoomListItemRes(
                room.getRoomId(),
                opponent,
                post,
                room.getLastMessagePreview(),
                room.getLastMessageAt(),
                unreadCount
        );
    }
}
