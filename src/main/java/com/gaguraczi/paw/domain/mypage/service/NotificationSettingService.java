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
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationSettingService {

    private final NotificationSettingRepository notificationSettingRepository;
    private final SecurityUtils securityUtils;

    @Transactional
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
                .orElseGet(() -> createDefault(user));
    }

    /** 동시 lazy 생성 요청이 uid unique 제약에 충돌하면, 먼저 커밋된 행을 다시 조회해 복구한다. */
    private NotificationSetting createDefault(User user) {
        NotificationSetting setting = NotificationSetting.builder()
                .user(user)
                .todoAlarm(true)
                .healthAlarm(true)
                .aiAnalysisAlarm(true)
                .communityAlarm(true)
                .chatAlarm(false)
                .benefitAlarm(false)
                .dndEnabled(true)
                .dndStart(LocalTime.of(22, 0))
                .dndEnd(LocalTime.of(7, 0))
                .build();
        try {
            return notificationSettingRepository.save(setting);
        } catch (DataIntegrityViolationException e) {
            return notificationSettingRepository.findByUser(user)
                    .orElseThrow(() -> e);
        }
    }
}
