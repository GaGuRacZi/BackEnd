package com.gaguraczi.paw.domain.chat.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "채팅 메시지 타입")
public enum MessageType {
    @Schema(description = "텍스트. data.content 필수")
    TEXT,
    @Schema(description = "이미지. multipart image 파트 필수")
    IMAGE
}
