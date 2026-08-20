package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.support.NotificationSettingCopy;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalTime;
import java.util.List;

public record NotificationSettingRes(
        Boolean todoAlarm,
        Boolean healthAlarm,
        Boolean aiAnalysisAlarm,
        Boolean communityAlarm,
        Boolean chatAlarm,
        Boolean benefitAlarm,
        Boolean dndEnabled,
        LocalTime dndStart,
        LocalTime dndEnd,
        @Schema(description = "화면 표시용 토글 목록 (Figma 순서·카피)")
        List<Item> items,
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

    public record Item(
            String key,
            String title,
            String description,
            boolean enabled
    ) {
    }

    public record Dnd(
            boolean enabled,
            LocalTime start,
            LocalTime end,
            String title,
            String description
    ) {
    }
}
