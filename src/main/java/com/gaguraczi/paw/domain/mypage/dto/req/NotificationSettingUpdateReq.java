package com.gaguraczi.paw.domain.mypage.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(description = "알림 설정 수정 요청 (보낸 필드만 반영, 개별/일괄 수정 겸용)")
@DndWindowPair
public record NotificationSettingUpdateReq(
        @Schema(description = "할 일 알림", example = "true")
        Boolean todoAlarm,

        @Schema(description = "건강 이상 알림 (방해 금지 시간대 예외)", example = "true")
        Boolean healthAlarm,

        @Schema(description = "AI 분석 완료 알림", example = "true")
        Boolean aiAnalysisAlarm,

        @Schema(description = "커뮤니티 알림", example = "true")
        Boolean communityAlarm,

        @Schema(description = "채팅 알림", example = "true")
        Boolean chatAlarm,

        @Schema(description = "혜택/이벤트 알림", example = "false")
        Boolean benefitAlarm,

        @Schema(description = "방해 금지 시간대 사용 여부", example = "true")
        Boolean dndEnabled,

        @Schema(description = "방해 금지 시작 시각", example = "22:00")
        LocalTime dndStart,

        @Schema(description = "방해 금지 종료 시각", example = "07:00")
        LocalTime dndEnd
) {
}
