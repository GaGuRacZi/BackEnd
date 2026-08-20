package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalTime;

@Component
public class NotificationPolicy {

    private final Clock clock;

    public NotificationPolicy(Clock clock) {
        this.clock = clock;
    }

    public boolean allowInbox(NotificationSetting setting, NotificationCategory category) {
        return isChannelEnabled(setting, category);
    }

    public boolean allowFcm(NotificationSetting setting, NotificationCategory category) {
        return allowFcm(setting, category, false, LocalTime.now(clock));
    }

    public boolean allowFcm(NotificationSetting setting, NotificationCategory category, boolean healthException) {
        return allowFcm(setting, category, healthException, LocalTime.now(clock));
    }

    public static boolean isChannelEnabled(NotificationSetting setting, NotificationCategory category) {
        if (setting == null || category == null) {
            return false;
        }
        return switch (category) {
            case TODO -> Boolean.TRUE.equals(setting.getTodoAlarm());
            case AI -> Boolean.TRUE.equals(setting.getAiAnalysisAlarm());
            case COMMUNITY -> Boolean.TRUE.equals(setting.getCommunityAlarm());
            case CHAT -> Boolean.TRUE.equals(setting.getChatAlarm());
            case EMERGENCY -> true;
        };
    }

    public static boolean isDndActive(NotificationSetting setting, LocalTime now) {
        if (setting == null || !Boolean.TRUE.equals(setting.getDndEnabled()) || now == null) {
            return false;
        }
        return inDndWindow(now, setting.getDndStart(), setting.getDndEnd());
    }

    /**
     * 카테고리 off면 FCM 스킵. DND면 FCM 스킵. {@code healthException}이면 건강 이상 알림만 DND를 뚫는다.
     */
    public static boolean allowFcm(
            NotificationSetting setting,
            NotificationCategory category,
            boolean healthException,
            LocalTime now
    ) {
        if (!isChannelEnabled(setting, category)) {
            return false;
        }
        if (!isDndActive(setting, now)) {
            return true;
        }
        return healthException && Boolean.TRUE.equals(setting.getHealthAlarm());
    }

    static boolean inDndWindow(LocalTime now, LocalTime start, LocalTime end) {
        if (start == null || end == null) {
            return false;
        }
        if (start.equals(end)) {
            return true;
        }
        if (start.isBefore(end)) {
            return !now.isBefore(start) && now.isBefore(end);
        }
        return !now.isBefore(start) || now.isBefore(end);
    }
}
