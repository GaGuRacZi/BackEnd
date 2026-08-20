package com.gaguraczi.paw.domain.chat.dto.req;

import com.gaguraczi.paw.domain.chat.enums.MessageType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "메시지 전송 JSON(data 파트). TEXT면 content 필수, IMAGE면 content를 보내지 않고 image 파일을 붙입니다.")
public record ChatMessageSendReq(
        @Schema(description = "TEXT | IMAGE", example = "TEXT", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull MessageType type,
        @Schema(description = "TEXT일 때 본문. IMAGE면 생략/null", example = "네 가능해요")
        String content
) {
}
