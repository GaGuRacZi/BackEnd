package com.gaguraczi.paw.domain.chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "채팅방 생성/조회 요청")
public record ChatRoomCreateReq(
        @Schema(example = "10") @NotNull @Positive Long postId
) {
}
