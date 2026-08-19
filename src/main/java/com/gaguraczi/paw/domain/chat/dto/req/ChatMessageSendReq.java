package com.gaguraczi.paw.domain.chat.dto.req;

import com.gaguraczi.paw.domain.chat.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메시지 전송 요청. TEXT면 content 필수, IMAGE면 image 파일 필수(별도 multipart part).")
public record ChatMessageSendReq(
        @Schema(example = "TEXT") @NotNull MessageType type,
        @Schema(example = "네 가능해요") String content
) {
}
