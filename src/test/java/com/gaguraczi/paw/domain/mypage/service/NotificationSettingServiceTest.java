package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.NotificationSettingUpdateReq;
import com.gaguraczi.paw.domain.mypage.entity.NotificationSetting;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.NotificationSettingRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationSettingServiceTest {

    @Mock
    private NotificationSettingRepository notificationSettingRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private NotificationSettingService notificationSettingService;

    private final User user = User.builder().uid(UUID.randomUUID()).build();

    @Test
    void 방해금지_시작시각만_보내면_예외가_발생한다() {
        when(securityUtils.currentUser()).thenReturn(user);
        when(notificationSettingRepository.findByUser(user))
                .thenReturn(Optional.of(NotificationSetting.builder().user(user).build()));

        NotificationSettingUpdateReq req = new NotificationSettingUpdateReq(
                null, null, null, null, null, null, null, LocalTime.of(22, 0), null
        );

        assertThatThrownBy(() -> notificationSettingService.update(req))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(MypageErrorCode.NOTIFICATION_SETTING_INVALID);
    }
}
