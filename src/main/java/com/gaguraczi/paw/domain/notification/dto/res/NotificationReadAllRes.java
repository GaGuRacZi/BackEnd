package com.gaguraczi.paw.domain.notification.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "모두 읽음 처리 결과")
public record NotificationReadAllRes(
        @Schema(description = "이번에 읽음으로 바뀐 알림 수. 이미 모두 읽었으면 0", example = "5")
        int updatedCount
) {
}
