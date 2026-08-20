package com.gaguraczi.paw.domain.notification.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 탭 시 이동할 대상 유형. targetId와 함께 사용")
public enum NotificationTargetType {
    @Schema(description = "할 일. targetId=todoId")
    TODO,
    @Schema(description = "진료. targetId=visitId")
    VISIT,
    @Schema(description = "커뮤니티 게시글. targetId=postId")
    POST,
    @Schema(description = "채팅방. targetId=roomId. GET /chat/rooms/{roomId}")
    CHAT_ROOM,
    @Schema(description = "지도/위치")
    MAP
}
