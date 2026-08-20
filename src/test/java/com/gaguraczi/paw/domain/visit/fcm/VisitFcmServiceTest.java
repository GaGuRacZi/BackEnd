package com.gaguraczi.paw.domain.visit.fcm;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.service.NotificationInboxService;
import com.gaguraczi.paw.domain.notification.service.NotificationPolicy;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.fcm.FcmPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitFcmServiceTest {

    @Mock
    private NotificationSettingService notificationSettingService;
    @Mock
    private NotificationPolicy notificationPolicy;
    @Mock
    private NotificationInboxService notificationInboxService;
    @Mock
    private FcmPushService fcmPushService;

    @InjectMocks
    private VisitFcmService visitFcmService;

    @Test
    void READY는_Figma_카피와_기존_data를_쓴다() {
        User user = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        NotificationSetting setting = NotificationSetting.builder().user(user).aiAnalysisAlarm(true).build();
        when(notificationSettingService.getOrCreate(user)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.AI)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.AI)).thenReturn(true);

        visitFcmService.notifyStatus(user, VisitFcmService.TYPE_READY, 5L, 8L);

        verify(notificationInboxService).insert(
                eq(user.getUid()),
                eq(NotificationCategory.AI),
                eq("AI 진료 요약이 완료됐어요"),
                eq("진료 녹음 분석 결과 확인 가능"),
                eq(NotificationTargetType.VISIT),
                eq(5L),
                eq(null),
                eq("요약 보기")
        );
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(fcmPushService).send(eq("tok"), eq("AI 진료 요약이 완료됐어요"), eq("진료 녹음 분석 결과 확인 가능"), data.capture());
        assertThat(data.getValue())
                .containsEntry("type", "VISIT_READY")
                .containsEntry("visitId", "5")
                .containsEntry("petId", "8");
    }

    @Test
    void DND면_인박스만_남기고_FCM은_스킵한다() {
        User user = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        NotificationSetting setting = NotificationSetting.builder().user(user).aiAnalysisAlarm(true).dndEnabled(true).build();
        when(notificationSettingService.getOrCreate(user)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.AI)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.AI)).thenReturn(false);

        visitFcmService.notifyStatus(user, VisitFcmService.TYPE_FAILED, 5L, 8L);

        verify(notificationInboxService).insert(
                eq(user.getUid()),
                eq(NotificationCategory.AI),
                eq("AI 진료 요약에 실패했어요"),
                eq("다시 시도하거나 녹음을 확인해 주세요"),
                eq(NotificationTargetType.VISIT),
                eq(5L),
                eq(null),
                eq("요약 보기")
        );
        verify(fcmPushService, never()).send(any(), any(), any(), any());
    }
}
