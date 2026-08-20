package com.gaguraczi.paw.domain.chat.fcm;

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
class ChatFcmServiceTest {

    @Mock
    private NotificationSettingService notificationSettingService;
    @Mock
    private NotificationPolicy notificationPolicy;
    @Mock
    private NotificationInboxService notificationInboxService;
    @Mock
    private FcmPushService fcmPushService;

    @InjectMocks
    private ChatFcmService chatFcmService;

    @Test
    void 상대방에게_인박스와_FCM을_보낸다() {
        UUID senderUid = UUID.randomUUID();
        User opponent = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        NotificationSetting setting = NotificationSetting.builder().user(opponent).chatAlarm(true).build();
        when(notificationSettingService.getOrCreate(opponent)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.CHAT)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.CHAT)).thenReturn(true);

        chatFcmService.notifyMessage(opponent, senderUid, "초코님의 메시지", "나눔 가능할까요?", 12L, 33L, 90L);

        verify(notificationInboxService).insert(
                eq(opponent.getUid()),
                eq(NotificationCategory.CHAT),
                eq("초코님의 메시지"),
                eq("나눔 가능할까요?"),
                eq(NotificationTargetType.CHAT_ROOM),
                eq(12L),
                eq(null),
                eq("채팅 보기")
        );
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(fcmPushService).send(eq("tok"), eq("초코님의 메시지"), eq("나눔 가능할까요?"), data.capture());
        assertThat(data.getValue())
                .containsEntry("type", "CHAT_MESSAGE")
                .containsEntry("category", "CHAT")
                .containsEntry("roomId", "12")
                .containsEntry("postId", "33")
                .containsEntry("senderId", senderUid.toString())
                .containsEntry("messageId", "90");
    }

    @Test
    void chatAlarm이_꺼져_있으면_스킵한다() {
        User opponent = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        NotificationSetting setting = NotificationSetting.builder().user(opponent).chatAlarm(false).build();
        when(notificationSettingService.getOrCreate(opponent)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.CHAT)).thenReturn(false);

        chatFcmService.notifyMessage(opponent, UUID.randomUUID(), "초코님의 메시지", "hi", 1L, 2L, 3L);

        verify(notificationInboxService, never()).insert(any(), any(), any(), any(), any(), any(), any(), any());
        verify(fcmPushService, never()).send(any(), any(), any(), any());
    }

    @Test
    void DND면_인박스만_남기고_FCM은_스킵한다() {
        User opponent = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        NotificationSetting setting = NotificationSetting.builder().user(opponent).chatAlarm(true).dndEnabled(true).build();
        when(notificationSettingService.getOrCreate(opponent)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.CHAT)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.CHAT)).thenReturn(false);

        chatFcmService.notifyMessage(opponent, UUID.randomUUID(), "초코님의 메시지", "hi", 1L, 2L, 3L);

        verify(notificationInboxService).insert(
                eq(opponent.getUid()),
                eq(NotificationCategory.CHAT),
                eq("초코님의 메시지"),
                eq("hi"),
                eq(NotificationTargetType.CHAT_ROOM),
                eq(1L),
                eq(null),
                eq("채팅 보기")
        );
        verify(fcmPushService, never()).send(any(), any(), any(), any());
    }

    @Test
    void 자기_자신에게는_보내지_않는다() {
        UUID uid = UUID.randomUUID();
        User me = User.builder().uid(uid).pushToken("tok").build();

        chatFcmService.notifyMessage(me, uid, "나님의 메시지", "hi", 1L, 2L, 3L);

        verify(notificationInboxService, never()).insert(any(), any(), any(), any(), any(), any(), any(), any());
        verify(fcmPushService, never()).send(any(), any(), any(), any());
    }
}
