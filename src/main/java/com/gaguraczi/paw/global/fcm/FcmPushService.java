package com.gaguraczi.paw.global.fcm;

import com.google.firebase.FirebaseApp;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

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
}
