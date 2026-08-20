package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.support.NotificationSettingCopy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

@Schema(description = "알림 설정. 토글 값은 boolean 필드와 items[].key가 1:1입니다. PATCH 시 boolean 필드명을 사용하세요.")
public record NotificationSettingRes(
        @Schema(description = "할 일 알림", example = "true")
        Boolean todoAlarm,
        @Schema(description = "건강 이상 알림. 방해 금지 시간에도 FCM 예외 발송 대상", example = "true")
        Boolean healthAlarm,
        @Schema(description = "AI 분석 완료 알림", example = "true")
        Boolean aiAnalysisAlarm,
        @Schema(description = "커뮤니티 알림 (댓글·답글·거래 문의)", example = "true")
        Boolean communityAlarm,
        @Schema(description = "채팅 알림. 기본 false. true여야 상대 메시지 인박스·FCM이 나갑니다", example = "false")
        Boolean chatAlarm,
        @Schema(description = "혜택/이벤트 알림", example = "false")
        Boolean benefitAlarm,
        @Schema(description = "방해 금지 시간대 사용 여부", example = "true")
        Boolean dndEnabled,
        @Schema(description = "방해 금지 시작 시각 (HH:mm:ss). 종료보다 늦으면 자정 넘김으로 해석", example = "22:00:00", type = "string", format = "time")
        LocalTime dndStart,
        @Schema(description = "방해 금지 종료 시각 (HH:mm:ss)", example = "07:00:00", type = "string", format = "time")
        LocalTime dndEnd,
        @Schema(description = "화면 표시용 토글 목록 (Figma 순서·카피). key는 PATCH 필드명과 동일")
        List<Item> items,
        @Schema(description = "방해 금지 시간 화면용 카피/값")
        Dnd dnd
) {
    public static NotificationSettingRes from(NotificationSetting setting) {
        return new NotificationSettingRes(
                setting.getTodoAlarm(),
                setting.getHealthAlarm(),
                setting.getAiAnalysisAlarm(),
                setting.getCommunityAlarm(),
                setting.getChatAlarm(),
                setting.getBenefitAlarm(),
                setting.getDndEnabled(),
                setting.getDndStart(),
                setting.getDndEnd(),
                NotificationSettingCopy.items(setting),
                NotificationSettingCopy.dnd(setting)
        );
    }

    @Schema(name = "NotificationSettingItem", description = "알림 토글 한 줄")
    public record Item(
            @Schema(description = "PATCH 필드명", example = "todoAlarm")
            String key,
            @Schema(description = "화면 제목", example = "할 일 알림")
            String title,
            @Schema(description = "화면 설명", example = "오늘의 할 일과 복약 시간을 알려줘요")
            String description,
            @Schema(description = "켜짐 여부", example = "true")
            boolean enabled
    ) {
    }

    @Schema(name = "NotificationSettingDnd", description = "방해 금지 시간")
    public record Dnd(
            @Schema(description = "사용 여부", example = "true")
            boolean enabled,
            @Schema(description = "시작 시각", example = "22:00:00", type = "string", format = "time")
            LocalTime start,
            @Schema(description = "종료 시각", example = "07:00:00", type = "string", format = "time")
            LocalTime end,
            @Schema(description = "화면 제목", example = "방해 금지 시간")
            String title,
            @Schema(description = "화면 설명. 건강 이상 알림은 DND 예외", example = "건강 이상 알림은 받을 수 있어요.")
            String description
    ) {
    }
}
