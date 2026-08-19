package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "채팅 상대방 요약 정보")
public record ChatUserSummaryRes(
        UUID uid,
        @Schema(example = "길동이") String nickname,
        @Schema(example = "https://cdn.example.com/users/a.jpg") String profileUrl
) {
    public static ChatUserSummaryRes from(User user) {
        return new ChatUserSummaryRes(user.getUid(), user.getNickname(), user.getProfileUrl());
    }
}
