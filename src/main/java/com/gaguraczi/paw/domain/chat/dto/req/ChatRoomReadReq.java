package com.gaguraczi.paw.domain.chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "읽음 처리 요청")
public record ChatRoomReadReq(
        @Schema(example = "501") @NotNull Long lastReadMessageId
) {
}
