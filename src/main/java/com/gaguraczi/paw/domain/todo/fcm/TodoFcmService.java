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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TodoFcmService {

    private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm");

    private final NotificationSettingService notificationSettingService;
    private final NotificationPolicy notificationPolicy;
    private final NotificationInboxService notificationInboxService;
    private final FcmPushService fcmPushService;

    public void sendReminder(TodoDateEntity todoDate) {
        TodoEntity todo = todoDate.getTodo();
        LocalTime todoTime = todo.getTodoTime();
        if (todoTime == null) {
            return;
        }
        User user = todo.getUser();
        NotificationSetting setting = notificationSettingService.getOrCreate(user);
        if (!notificationPolicy.allowInbox(setting, NotificationCategory.TODO)) {
            return;
        }
        String title = todo.getTodo() + " 체크가 필요해요";
        String body = "오늘 " + todoTime.format(TIME) + " · 미완료 상태예요";
        notificationInboxService.insert(
                user.getUid(),
                NotificationCategory.TODO,
                title,
                body,
                NotificationTargetType.TODO,
                todo.getTodoId(),
                todoDate.getTodoDateId(),
                "할 일 보기"
        );
        if (notificationPolicy.allowFcm(setting, NotificationCategory.TODO)) {
            fcmPushService.send(user.getPushToken(), title, body, Map.of(
                    "type", "TODO_REMINDER",
                    "todoId", String.valueOf(todo.getTodoId()),
                    "todoDateId", String.valueOf(todoDate.getTodoDateId())
            ));
        }
    }
}
