package com.gaguraczi.paw.domain.notification.dto.res;

import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "알림 인박스 아이템. 오늘/어제 그룹은 앱이 createdAt(KST)으로 나눕니다.")
public record NotificationItemRes(
        @Schema(description = "알림 ID", example = "101")
        Long id,
        @Schema(description = "TODO | AI | COMMUNITY | CHAT | EMERGENCY", example = "CHAT")
        NotificationCategory category,
        @Schema(description = "제목. CHAT이면 `{닉네임}님의 메시지`", example = "초코님의 메시지")
        String title,
        @Schema(description = "본문. CHAT 텍스트는 최대 100자, 이미지는 '사진을 보냈습니다'", example = "나눔 가능할까요?")
        String body,
        @JsonProperty("isRead")
        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,
        @Schema(description = "생성 시각", example = "2026-08-20T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "CTA 라벨. CHAT은 '채팅 보기'", example = "채팅 보기")
        String ctaLabel,
        @Schema(description = "TODO | VISIT | POST | CHAT_ROOM | MAP", example = "CHAT_ROOM")
        NotificationTargetType targetType,
        @Schema(description = "CHAT_ROOM=roomId, POST=postId, VISIT=visitId, TODO=todoId", example = "12")
        Long targetId
) {
    public static NotificationItemRes from(Notification notification) {
        return new NotificationItemRes(
                notification.getNotificationId(),
                notification.getCategory(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getCtaLabel(),
                notification.getTargetType(),
                notification.getTargetId()
        );
    }
}
