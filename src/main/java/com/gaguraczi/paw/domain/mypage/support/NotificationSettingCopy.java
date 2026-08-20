package com.gaguraczi.paw.domain.mypage.support;

import com.gaguraczi.paw.domain.mypage.dto.res.NotificationSettingRes;
import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;

import java.util.List;

/** Figma 알림 설정 화면(476:1356) 카피. 레이어명(복약·병원예약)이 아니라 보이는 텍스트가 계약이다. */
public final class NotificationSettingCopy {

    private NotificationSettingCopy() {
    }

    public static List<NotificationSettingRes.Item> items(NotificationSetting setting) {
        return List.of(
                item("todoAlarm", "할 일 알림", "오늘의 할 일과 복약 시간을 알려줘요", setting.getTodoAlarm()),
                item("healthAlarm", "건강 이상 알림", "기록에서 주의가 필요한 변화를 알려줘요", setting.getHealthAlarm()),
                item("aiAnalysisAlarm", "AI 분석 완료 알림", "진료 요약과 OCR 분석 완료를 알려줘요", setting.getAiAnalysisAlarm()),
                item("communityAlarm", "커뮤니티 알림", "댓글, 답글, 거래 문의를 알려줘요", setting.getCommunityAlarm()),
                item("chatAlarm", "채팅 알림", "새 메시지와 거래 대화를 알려줘요", setting.getChatAlarm()),
                item("benefitAlarm", "혜택 이벤트 알림", "PAW 혜택과 이벤트 소식을 받아요", setting.getBenefitAlarm())
        );
    }

    public static NotificationSettingRes.Dnd dnd(NotificationSetting setting) {
        return new NotificationSettingRes.Dnd(
                Boolean.TRUE.equals(setting.getDndEnabled()),
                setting.getDndStart(),
                setting.getDndEnd(),
                "방해 금지 시간",
                "건강 이상 알림은 받을 수 있어요."
        );
    }

    private static NotificationSettingRes.Item item(String key, String title, String description, Boolean enabled) {
        return new NotificationSettingRes.Item(key, title, description, Boolean.TRUE.equals(enabled));
    }
}
