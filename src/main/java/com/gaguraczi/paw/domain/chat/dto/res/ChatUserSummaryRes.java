package com.gaguraczi.paw.domain.chat.dto.res;

import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.UUID;

@Schema(description = "채팅 상대 요약")
public record ChatUserSummaryRes(
        @Schema(description = "상대 uid", example = "11111111-1111-1111-1111-111111111111")
        UUID uid,
        @Schema(description = "닉네임. 알림 제목 `{nickname}님의 메시지`에 사용", example = "초코")
        String nickname,
        @Schema(description = "프로필 이미지 URL", example = "https://cdn.example.com/users/choco.jpg")
        String profileUrl
) {
    public static ChatUserSummaryRes from(User user) {
        return new ChatUserSummaryRes(user.getUid(), user.getNickname(), user.getProfileUrl());
    }
}
