package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 get-or-create 결과. 상대·글 카드는 GET /chat/rooms/{roomId} 로 조회합니다.")
public record ChatRoomCreateRes(
        @Schema(description = "채팅방 ID. 이미 있어도 같은 값", example = "12")
        Long roomId,
        @Schema(description = "방 최초 생성 시각. 재호출이어도 처음 만든 시각", example = "2026-08-20T11:00:00")
        LocalDateTime createdAt
) {
    public static ChatRoomCreateRes from(ChatRoom room) {
        return new ChatRoomCreateRes(room.getRoomId(), room.getCreatedAt());
    }
}
