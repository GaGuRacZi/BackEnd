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
        @Schema(description = "카테고리", example = "TODO")
        NotificationCategory category,
        @Schema(description = "제목", example = "슬개골 영양제 체크가 필요해요")
        String title,
        @Schema(description = "본문", example = "오늘 09:00 · 미완료 상태예요")
        String body,
        @JsonProperty("isRead")
        @Schema(description = "읽음 여부", example = "false")
        boolean isRead,
        @Schema(description = "생성 시각", example = "2026-08-20T09:00:00")
        LocalDateTime createdAt,
        @Schema(description = "CTA 버튼 라벨", example = "할 일 보기")
        String ctaLabel,
        @Schema(description = "딥링크 대상 유형", example = "TODO")
        NotificationTargetType targetType,
        @Schema(description = "딥링크 대상 ID (할 일/진료/게시글 등)", example = "15")
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
