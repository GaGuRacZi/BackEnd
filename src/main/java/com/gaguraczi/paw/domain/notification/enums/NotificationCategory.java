package com.gaguraczi.paw.domain.notification.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "알림 카테고리. 목록 필터와 설정 채널 매핑에 사용")
public enum NotificationCategory {
    @Schema(description = "할 일 (todoAlarm)")
    TODO,
    @Schema(description = "AI 분석 (aiAnalysisAlarm)")
    AI,
    @Schema(description = "커뮤니티 (communityAlarm)")
    COMMUNITY,
    @Schema(description = "채팅 (chatAlarm). 기본 OFF. 메시지 전송 시 상대 인박스·FCM")
    CHAT,
    @Schema(description = "긴급/건강 이상. 인박스 적재는 설정 off여도 허용")
    EMERGENCY
}
