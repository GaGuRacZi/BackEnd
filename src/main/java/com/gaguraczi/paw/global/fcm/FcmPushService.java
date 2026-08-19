package com.gaguraczi.paw.global.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class FcmPushService {

    public void sendVisitStatus(String pushToken, String type, Long visitId, Long petId, String title, String body) {
        if (pushToken == null || pushToken.isBlank()) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase is not initialized; skip FCM type={}", type);
            return;
        }
        try {
            Message.Builder builder = Message.builder()
                    .setToken(pushToken.trim())
                    .putData("type", type)
                    .putData("visitId", String.valueOf(visitId))
                    .putData("petId", String.valueOf(petId));
            if (title != null && body != null) {
                builder.setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
            }
            FirebaseMessaging.getInstance().send(builder.build());
        } catch (Exception e) {
            log.warn("FCM send skipped type={} visitId={}: {}", type, visitId, e.getMessage());
        }
    }

    /** 범용 발송. data에는 딥링크에 필요한 최소 정보만, 문구는 title/body로 전달한다. */
    public SendResult send(String token, String title, String body, Map<String, String> data) {
        if (token == null || token.isBlank()) {
            return SendResult.INVALID_TOKEN;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase is not initialized; skip FCM send");
            return SendResult.OTHER_FAILURE;
        }
        try {
            Message.Builder builder = Message.builder().setToken(token.trim());
            if (data != null) {
                data.forEach(builder::putData);
            }
            if (title != null && body != null) {
                builder.setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
            }
            FirebaseMessaging.getInstance().send(builder.build());
            return SendResult.SUCCESS;
        } catch (FirebaseMessagingException e) {
            MessagingErrorCode errorCode = e.getMessagingErrorCode();
            if (errorCode == MessagingErrorCode.UNREGISTERED || errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
                log.info("FCM token invalid, will be pruned: {}", e.getMessage());
                return SendResult.INVALID_TOKEN;
            }
            log.warn("FCM send failed: {}", e.getMessage());
            return SendResult.OTHER_FAILURE;
        } catch (Exception e) {
            log.warn("FCM send failed: {}", e.getMessage());
            return SendResult.OTHER_FAILURE;
        }
    }

    public enum SendResult {
        SUCCESS,
        /** 만료·무효 토큰. 호출측에서 DeviceToken을 정리해야 한다. */
        INVALID_TOKEN,
        OTHER_FAILURE
    }
}
