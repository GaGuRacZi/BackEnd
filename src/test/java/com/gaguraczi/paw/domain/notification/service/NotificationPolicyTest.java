package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.users.entity.User;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationPolicyTest {

    private final User user = User.builder().uid(UUID.randomUUID()).build();

    @Test
    void 카테고리_off면_인박스와_FCM을_막는다() {
        NotificationSetting setting = on()
                .todoAlarm(false)
                .build();

        assertThat(NotificationPolicy.isChannelEnabled(setting, NotificationCategory.TODO)).isFalse();
        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.TODO, false, LocalTime.of(12, 0)))
                .isFalse();
    }

    @Test
    void DND_구간이면_FCM만_막는다() {
        NotificationSetting setting = on().dndEnabled(true).build();

        assertThat(NotificationPolicy.isChannelEnabled(setting, NotificationCategory.AI)).isTrue();
        assertThat(NotificationPolicy.isDndActive(setting, LocalTime.of(22, 0))).isTrue();
        assertThat(NotificationPolicy.isDndActive(setting, LocalTime.of(6, 59))).isTrue();
        assertThat(NotificationPolicy.isDndActive(setting, LocalTime.of(7, 0))).isFalse();
        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.AI, false, LocalTime.of(23, 0)))
                .isFalse();
        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.AI, false, LocalTime.of(12, 0)))
                .isTrue();
    }

    @Test
    void 건강이상은_DND여도_FCM을_보낸다() {
        NotificationSetting setting = on().dndEnabled(true).healthAlarm(true).build();

        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.TODO, true, LocalTime.of(23, 30)))
                .isTrue();
    }

    @Test
    void 건강이상_off면_DND예외여도_FCM을_막는다() {
        NotificationSetting setting = on().dndEnabled(true).healthAlarm(false).build();

        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.TODO, true, LocalTime.of(23, 30)))
                .isFalse();
    }

    @Test
    void todoAlarm이_꺼져_있으면_건강이상_예외여도_FCM을_막는다() {
        NotificationSetting setting = on()
                .todoAlarm(false)
                .healthAlarm(true)
                .dndEnabled(true)
                .build();

        assertThat(NotificationPolicy.allowFcm(setting, NotificationCategory.TODO, true, LocalTime.of(23, 30)))
                .isFalse();
    }

    @Test
    void 심야_DND는_시작이_종료보다_늦다() {
        assertThat(NotificationPolicy.inDndWindow(LocalTime.of(21, 59), LocalTime.of(22, 0), LocalTime.of(7, 0)))
                .isFalse();
        assertThat(NotificationPolicy.inDndWindow(LocalTime.of(22, 0), LocalTime.of(22, 0), LocalTime.of(7, 0)))
                .isTrue();
        assertThat(NotificationPolicy.inDndWindow(LocalTime.of(3, 0), LocalTime.of(22, 0), LocalTime.of(7, 0)))
                .isTrue();
    }

    private NotificationSetting.NotificationSettingBuilder<?, ?> on() {
        return NotificationSetting.builder()
                .user(user)
                .todoAlarm(true)
                .healthAlarm(true)
                .aiAnalysisAlarm(true)
                .communityAlarm(true)
                .chatAlarm(false)
                .benefitAlarm(false)
                .dndEnabled(false)
                .dndStart(LocalTime.of(22, 0))
                .dndEnd(LocalTime.of(7, 0));
    }
}
