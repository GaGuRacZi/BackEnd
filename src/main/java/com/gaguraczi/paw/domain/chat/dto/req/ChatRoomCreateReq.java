package com.gaguraczi.paw.domain.chat.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@Schema(description = "채팅방 get-or-create. 장터(MARKET) 글 ID. 요청자가 buyer가 됩니다.")
public record ChatRoomCreateReq(
        @Schema(description = "장터 게시글 ID", example = "33", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull @Positive Long postId
) {
}
