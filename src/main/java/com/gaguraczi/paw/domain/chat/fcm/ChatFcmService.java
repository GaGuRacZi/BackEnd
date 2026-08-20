package com.gaguraczi.paw.domain.chat.fcm;

import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.service.NotificationInboxService;
import com.gaguraczi.paw.domain.notification.service.NotificationPolicy;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.fcm.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatFcmService {

    public static final String TYPE_MESSAGE = "CHAT_MESSAGE";
    static final String CTA = "채팅 보기";

    private final NotificationSettingService notificationSettingService;
    private final NotificationPolicy notificationPolicy;
    private final NotificationInboxService notificationInboxService;
    private final FcmPushService fcmPushService;

    public void notifyMessage(
            User opponent,
            UUID senderUid,
            String title,
            String body,
            Long roomId,
            Long postId,
            Long messageId
    ) {
        try {
            if (opponent == null || Objects.equals(opponent.getUid(), senderUid)) {
                return;
            }
            NotificationSetting setting = notificationSettingService.getOrCreate(opponent);
            if (!notificationPolicy.allowInbox(setting, NotificationCategory.CHAT)) {
                return;
            }
            notificationInboxService.insert(
                    opponent.getUid(),
                    NotificationCategory.CHAT,
                    title,
                    body,
                    NotificationTargetType.CHAT_ROOM,
                    roomId,
                    null,
                    CTA
            );
            if (notificationPolicy.allowFcm(setting, NotificationCategory.CHAT)) {
                fcmPushService.send(opponent.getPushToken(), title, body, fcmData(roomId, postId, senderUid, messageId));
            }
        } catch (Exception e) {
            log.warn("Chat FCM skipped roomId={}: {}", roomId, e.getMessage());
        }
    }

    private static Map<String, String> fcmData(Long roomId, Long postId, UUID senderUid, Long messageId) {
        Map<String, String> data = new HashMap<>();
        data.put("type", TYPE_MESSAGE);
        data.put("category", "CHAT");
        if (roomId != null) {
            data.put("roomId", String.valueOf(roomId));
        }
        if (postId != null) {
            data.put("postId", String.valueOf(postId));
        }
        if (senderUid != null) {
            data.put("senderId", String.valueOf(senderUid));
        }
        if (messageId != null) {
            data.put("messageId", String.valueOf(messageId));
        }
        return data;
    }
}
