package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.notification.entity.DeviceToken;
import com.gaguraczi.paw.domain.notification.enums.DevicePlatform;
import com.gaguraczi.paw.domain.notification.repository.DeviceTokenRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {

    private final DeviceTokenRepository deviceTokenRepository;
    private final SecurityUtils securityUtils;

    /** upsert. 토큰이 다른 유저 소유였다면 현재 유저로 소유자를 이전한다(재설치·계정전환 대응). */
    @Transactional
    public void register(String token, DevicePlatform platform) {
        User me = securityUtils.currentUser();
        DeviceToken deviceToken = deviceTokenRepository.findByToken(token)
                .orElseGet(() -> deviceTokenRepository.save(
                        DeviceToken.builder()
                                .user(me)
                                .token(token)
                                .platform(platform)
                                .build()
                ));
        if (!deviceToken.getUser().getUid().equals(me.getUid())) {
            deviceToken.reassignTo(me);
        }
        deviceToken.changePlatform(platform);
    }

    @Transactional
    public void unregister(String token) {
        User me = securityUtils.currentUser();
        deviceTokenRepository.deleteByTokenAndUser_Uid(token, me.getUid());
    }
}
