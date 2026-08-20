package com.gaguraczi.paw.domain.mypage.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;

@Schema(
        description = """
                알림 설정 부분 수정. 보낸 필드만 반영합니다.
                dndStart/dndEnd는 둘 다 보내거나 둘 다 생략해야 합니다(한쪽만 보내면 MYPAGE_400_1).
                자정을 넘는 구간(예: 22:00~07:00)을 허용합니다.
                """
)
@DndWindowPair
public record NotificationSettingUpdateReq(
        @Schema(description = "할 일 알림", example = "true")
        Boolean todoAlarm,

        @Schema(description = "건강 이상 알림. 방해 금지 시간에도 FCM 예외", example = "true")
        Boolean healthAlarm,

        @Schema(description = "AI 분석 완료 알림", example = "true")
        Boolean aiAnalysisAlarm,

        @Schema(description = "커뮤니티 알림", example = "true")
        Boolean communityAlarm,

        @Schema(description = "채팅 알림. true여야 상대 메시지 인박스·FCM이 나갑니다. 기본값은 false", example = "true")
        Boolean chatAlarm,

        @Schema(description = "혜택/이벤트 알림", example = "false")
        Boolean benefitAlarm,

        @Schema(description = "방해 금지 시간대 사용 여부", example = "true")
        Boolean dndEnabled,

        @Schema(description = "방해 금지 시작 시각. dndEnd와 함께 전송", example = "22:00", type = "string", format = "time")
        LocalTime dndStart,

        @Schema(description = "방해 금지 종료 시각. dndStart와 함께 전송", example = "07:00", type = "string", format = "time")
        LocalTime dndEnd
) {
}
