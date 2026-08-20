package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.chat.entity.ChatRoom;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅방 상세. 상단 상대 프로필 + 장터 글 카드.")
public record ChatRoomDetailRes(
        @Schema(description = "채팅방 ID", example = "12")
        Long roomId,
        @Schema(description = "나 아닌 참여자")
        ChatUserSummaryRes opponent,
        @Schema(description = "장터 글 요약. 삭제됐으면 deleted=true")
        ChatPostSummaryRes post
) {
    public static ChatRoomDetailRes of(ChatRoom room, ChatUserSummaryRes opponent, ChatPostSummaryRes post) {
        return new ChatRoomDetailRes(room.getRoomId(), opponent, post);
    }
}
