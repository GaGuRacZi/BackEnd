package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 목록 한 줄. unreadCount는 상대가 보낸, 아직 안 읽은 메시지 수입니다.")
public record ChatRoomListItemRes(
        @Schema(description = "채팅방 ID", example = "12")
        Long roomId,
        ChatUserSummaryRes opponent,
        ChatPostSummaryRes post,
        @Schema(description = "마지막 메시지 미리보기. 이미지는 '사진을 보냈습니다', 텍스트는 최대 100자", example = "나눔 가능할까요?")
        String lastMessagePreview,
        @Schema(description = "마지막 메시지 시각. 메시지가 없으면 방 생성 시각", example = "2026-08-20T11:30:00")
        LocalDateTime lastMessageAt,
        @Schema(description = "상대 메시지 미읽음 수", example = "2")
        long unreadCount
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
