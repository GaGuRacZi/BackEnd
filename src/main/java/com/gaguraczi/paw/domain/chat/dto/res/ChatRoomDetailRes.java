package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상세 (상단 게시글 요약 카드 + 상대방 정보)")
public record ChatRoomDetailRes(
        Long roomId,
        ChatUserSummaryRes opponent,
        ChatPostSummaryRes post
) {
    public static ChatRoomDetailRes of(ChatRoom room, ChatUserSummaryRes opponent, ChatPostSummaryRes post) {
        return new ChatRoomDetailRes(room.getRoomId(), opponent, post);
    }
}
