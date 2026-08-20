package com.gaguraczi.paw.global.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class FcmPushService {

    private final FcmTokenClearer fcmTokenClearer;

    public void send(String pushToken, String title, String body, Map<String, String> data) {
        if (pushToken == null || pushToken.isBlank()) {
            return;
        }
        if (FirebaseApp.getApps().isEmpty()) {
            log.warn("Firebase is not initialized; skip FCM");
            return;
        }
        String token = pushToken.trim();
        try {
            Message.Builder builder = Message.builder().setToken(token);
            if (data != null) {
                data.forEach((key, value) -> {
                    if (key != null && value != null) {
                        builder.putData(key, value);
                    }
                });
            }
            if (title != null && body != null) {
                builder.setNotification(Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build());
            }
            FirebaseMessaging.getInstance().send(builder.build());
        } catch (FirebaseMessagingException e) {
            if (isUnregistered(e)) {
                fcmTokenClearer.clearByToken(token);
                log.warn("FCM send skipped: {}", e.getMessage());
                return;
            }
            log.warn("FCM send skipped: {}", e.getMessage());
            throw new IllegalStateException("FCM send failed", e);
        } catch (Exception e) {
            log.warn("FCM send skipped: {}", e.getMessage());
        }
    }

    private static boolean isUnregistered(FirebaseMessagingException e) {
        return e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED;
    }
}
