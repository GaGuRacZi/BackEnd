package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "채팅방 생성/조회(idempotent get-or-create) 결과")
public record ChatRoomCreateRes(
        Long roomId,
        LocalDateTime createdAt
) {
    public static ChatRoomCreateRes from(ChatRoom room) {
        return new ChatRoomCreateRes(room.getRoomId(), room.getCreatedAt());
    }
}
