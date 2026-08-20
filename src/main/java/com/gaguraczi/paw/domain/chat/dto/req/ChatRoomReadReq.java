package com.gaguraczi.paw.domain.chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "방 단위 읽음 처리. 이 방의 메시지 ID만 허용합니다.")
public record ChatRoomReadReq(
        @Schema(description = "이 방에서 읽은 마지막 메시지 ID. 더 큰 값만 저장됩니다.", example = "501", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull Long lastReadMessageId
) {
}
