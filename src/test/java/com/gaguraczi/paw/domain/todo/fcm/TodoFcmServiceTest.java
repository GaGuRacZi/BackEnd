package com.gaguraczi.paw.domain.todo.fcm;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.service.NotificationInboxService;
import com.gaguraczi.paw.domain.notification.service.NotificationPolicy;
import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.fcm.FcmPushService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoFcmServiceTest {

    @Mock
    private NotificationSettingService notificationSettingService;
    @Mock
    private NotificationPolicy notificationPolicy;
    @Mock
    private NotificationInboxService notificationInboxService;
    @Mock
    private FcmPushService fcmPushService;

    @InjectMocks
    private TodoFcmService todoFcmService;

    @Test
    void 투두_리마인더_카피와_data를_보낸다() {
        User user = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        TodoEntity todo = mock(TodoEntity.class);
        TodoDateEntity todoDate = mock(TodoDateEntity.class);
        NotificationSetting setting = NotificationSetting.builder().user(user).todoAlarm(true).build();
        when(todo.getTodo()).thenReturn("심장약 복용");
        when(todo.getTodoTime()).thenReturn(LocalTime.of(20, 0));
        when(todo.getTodoId()).thenReturn(7L);
        when(todo.getUser()).thenReturn(user);
        when(todoDate.getTodo()).thenReturn(todo);
        when(todoDate.getTodoDateId()).thenReturn(11L);
        when(notificationSettingService.getOrCreate(user)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.TODO)).thenReturn(true);
        when(notificationPolicy.allowFcm(setting, NotificationCategory.TODO)).thenReturn(true);

        todoFcmService.sendReminder(todoDate);

        verify(notificationInboxService).insert(
                eq(user.getUid()),
                eq(NotificationCategory.TODO),
                eq("심장약 복용 체크가 필요해요"),
                eq("오늘 20:00 · 미완료 상태예요"),
                eq(NotificationTargetType.TODO),
                eq(7L),
                eq(11L),
                eq("할 일 보기")
        );
        @SuppressWarnings("unchecked")
        ArgumentCaptor<Map<String, String>> data = ArgumentCaptor.forClass(Map.class);
        verify(fcmPushService).send(eq("tok"), eq("심장약 복용 체크가 필요해요"), eq("오늘 20:00 · 미완료 상태예요"), data.capture());
        assertThat(data.getValue()).containsEntry("type", "TODO_REMINDER")
                .containsEntry("todoId", "7")
                .containsEntry("todoDateId", "11");
    }

    @Test
    void todoAlarm이_꺼져_있으면_스킵한다() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        TodoEntity todo = mock(TodoEntity.class);
        TodoDateEntity todoDate = mock(TodoDateEntity.class);
        NotificationSetting setting = NotificationSetting.builder().user(user).todoAlarm(false).build();
        when(todo.getTodoTime()).thenReturn(LocalTime.of(20, 0));
        when(todo.getUser()).thenReturn(user);
        when(todoDate.getTodo()).thenReturn(todo);
        when(notificationSettingService.getOrCreate(user)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.TODO)).thenReturn(false);

        todoFcmService.sendReminder(todoDate);

        verify(notificationInboxService, never()).insert(any(), any(), any(), any(), any(), any(), any(), any());
        verify(fcmPushService, never()).send(any(), any(), any(), any());
    }

    @Test
    void 발송_실패를_숨기지_않는다() {
        User user = User.builder().uid(UUID.randomUUID()).pushToken("tok").build();
        TodoEntity todo = mock(TodoEntity.class);
        TodoDateEntity todoDate = mock(TodoDateEntity.class);
        NotificationSetting setting = NotificationSetting.builder().user(user).todoAlarm(true).build();
        when(todo.getTodo()).thenReturn("심장약 복용");
        when(todo.getTodoTime()).thenReturn(LocalTime.of(20, 0));
        when(todo.getTodoId()).thenReturn(7L);
        when(todo.getUser()).thenReturn(user);
        when(todoDate.getTodo()).thenReturn(todo);
        when(todoDate.getTodoDateId()).thenReturn(11L);
        when(notificationSettingService.getOrCreate(user)).thenReturn(setting);
        when(notificationPolicy.allowInbox(setting, NotificationCategory.TODO)).thenReturn(true);
        when(notificationInboxService.insert(
                eq(user.getUid()),
                eq(NotificationCategory.TODO),
                eq("심장약 복용 체크가 필요해요"),
                eq("오늘 20:00 · 미완료 상태예요"),
                eq(NotificationTargetType.TODO),
                eq(7L),
                eq(11L),
                eq("할 일 보기")
        )).thenThrow(new RuntimeException("inbox down"));

        assertThatThrownBy(() -> todoFcmService.sendReminder(todoDate))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("inbox down");
    }
}
