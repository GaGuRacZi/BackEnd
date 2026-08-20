package com.gaguraczi.paw.domain.visit.fcm;

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

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class VisitFcmService {

    public static final String TYPE_READY = "VISIT_READY";
    public static final String TYPE_FAILED = "VISIT_FAILED";

    static final String READY_TITLE = "AI 진료 요약이 완료됐어요";
    static final String READY_BODY = "진료 녹음 분석 결과 확인 가능";
    static final String FAILED_TITLE = "AI 진료 요약에 실패했어요";
    static final String FAILED_BODY = "다시 시도하거나 녹음을 확인해 주세요";

    private final NotificationSettingService notificationSettingService;
    private final NotificationPolicy notificationPolicy;
    private final NotificationInboxService notificationInboxService;
    private final FcmPushService fcmPushService;

    public void notifyStatus(User user, String type, Long visitId, Long petId) {
        if (user == null) {
            return;
        }
        NotificationSetting setting = notificationSettingService.getOrCreate(user);
        if (!notificationPolicy.allowInbox(setting, NotificationCategory.AI)) {
            return;
        }
        boolean ready = TYPE_READY.equals(type);
        String title = ready ? READY_TITLE : FAILED_TITLE;
        String body = ready ? READY_BODY : FAILED_BODY;
        try {
            notificationInboxService.insert(
                    user.getUid(),
                    NotificationCategory.AI,
                    title,
                    body,
                    NotificationTargetType.VISIT,
                    visitId,
                    null,
                    "요약 보기"
            );
            if (notificationPolicy.allowFcm(setting, NotificationCategory.AI)) {
                fcmPushService.send(user.getPushToken(), title, body, Map.of(
                        "type", type,
                        "visitId", String.valueOf(visitId),
                        "petId", String.valueOf(petId)
                ));
            }
        } catch (Exception e) {
            log.warn("Visit FCM skipped type={} visitId={}: {}", type, visitId, e.getMessage());
        }
    }
}
