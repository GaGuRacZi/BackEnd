package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.NotificationSettingUpdateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.NotificationSettingRes;
import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.NotificationSettingRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final SecurityUtils securityUtils;

    public NotificationSettingRes get() {
        User user = securityUtils.currentUser();
        return NotificationSettingRes.from(getOrCreate(user));
    }

    @Transactional
    public NotificationSettingRes update(NotificationSettingUpdateReq req) {
        User user = securityUtils.currentUser();
        NotificationSetting setting = getOrCreate(user);

        boolean dndStartTouched = req.dndStart() != null;
        boolean dndEndTouched = req.dndEnd() != null;
        if (dndStartTouched != dndEndTouched) {
            throw GeneralException.of(MypageErrorCode.NOTIFICATION_SETTING_INVALID);
        }

        setting.update(
                req.todoAlarm(),
                req.healthAlarm(),
                req.aiAnalysisAlarm(),
                req.communityAlarm(),
                req.chatAlarm(),
                req.benefitAlarm(),
                req.dndEnabled(),
                req.dndStart(),
                req.dndEnd()
        );
        return NotificationSettingRes.from(setting);
    }

    /** 온보딩 시점에 생성되지 않은 기존 유저를 위한 lazy 생성. 온보딩 흐름에서는 AuthService가 기본값으로 미리 생성한다. */
    @Transactional
    public NotificationSetting getOrCreate(User user) {
        return notificationSettingRepository.findByUser(user)
                .orElseGet(() -> notificationSettingRepository.save(
                        NotificationSetting.builder().user(user).build()
                ));
    }
}
