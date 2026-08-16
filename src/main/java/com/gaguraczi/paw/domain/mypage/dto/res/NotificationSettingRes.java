package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;

import java.time.LocalTime;

public record NotificationSettingRes(
        Boolean todoAlarm,
        Boolean healthAlarm,
        Boolean aiAnalysisAlarm,
        Boolean communityAlarm,
        Boolean chatAlarm,
        Boolean benefitAlarm,
        Boolean dndEnabled,
        LocalTime dndStart,
        LocalTime dndEnd
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
                setting.getDndEnd()
        );
    }
}
