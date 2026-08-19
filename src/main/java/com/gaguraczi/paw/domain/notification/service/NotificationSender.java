package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.notification.entity.DeviceToken;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.gate.NotificationGatePort;
import com.gaguraczi.paw.domain.notification.repository.DeviceTokenRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.fcm.FcmPushService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

/** 모든 FCM 알림이 거쳐가는 공통 발송 서비스. 게이팅 → 전체 디바이스 발송 → 무효 토큰 정리. */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationSender {

    private final DeviceTokenRepository deviceTokenRepository;
    private final FcmPushService fcmPushService;
    private final NotificationGatePort notificationGatePort;

    @Transactional
    public void send(User receiver, NotificationCategory category, String title, String body, Map<String, String> data) {
        if (!notificationGatePort.allow(receiver, category)) {
            return;
        }
        List<DeviceToken> tokens = deviceTokenRepository.findByUser_Uid(receiver.getUid());
        for (DeviceToken deviceToken : tokens) {
            FcmPushService.SendResult result = fcmPushService.send(deviceToken.getToken(), title, body, data);
            if (result == FcmPushService.SendResult.INVALID_TOKEN) {
                deviceTokenRepository.delete(deviceToken);
            }
        }
    }
}
