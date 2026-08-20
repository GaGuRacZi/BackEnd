package com.gaguraczi.paw.domain.notification.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "미읽음 알림 수")
public record NotificationUnreadCountRes(
        @Schema(description = "isRead=false 인 알림 개수", example = "3")
        long count
) {
}
