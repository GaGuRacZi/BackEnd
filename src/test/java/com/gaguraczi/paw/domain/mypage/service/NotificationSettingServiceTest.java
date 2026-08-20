package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.NotificationSettingUpdateReq;
import com.gaguraczi.paw.domain.mypage.dto.res.NotificationSettingRes;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
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

    @Test
    void 신규_설정_기본값은_채팅_혜택_off_DND_on이다() {
        when(securityUtils.currentUser()).thenReturn(user);
        when(notificationSettingRepository.findByUser(user)).thenReturn(Optional.empty());
        when(notificationSettingRepository.save(any(NotificationSetting.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        NotificationSettingRes res = notificationSettingService.get();

        assertThat(res.todoAlarm()).isTrue();
        assertThat(res.healthAlarm()).isTrue();
        assertThat(res.aiAnalysisAlarm()).isTrue();
        assertThat(res.communityAlarm()).isTrue();
        assertThat(res.chatAlarm()).isFalse();
        assertThat(res.benefitAlarm()).isFalse();
        assertThat(res.dndEnabled()).isTrue();
        assertThat(res.dndStart()).isEqualTo(LocalTime.of(22, 0));
        assertThat(res.dndEnd()).isEqualTo(LocalTime.of(7, 0));
        assertThat(res.items()).extracting(NotificationSettingRes.Item::key)
                .containsExactly(
                        "todoAlarm", "healthAlarm", "aiAnalysisAlarm",
                        "communityAlarm", "chatAlarm", "benefitAlarm"
                );
        assertThat(res.items()).extracting(NotificationSettingRes.Item::title)
                .containsExactly(
                        "할 일 알림", "건강 이상 알림", "AI 분석 완료 알림",
                        "커뮤니티 알림", "채팅 알림", "혜택 이벤트 알림"
                );
        assertThat(res.dnd().title()).isEqualTo("방해 금지 시간");
        assertThat(res.dnd().description()).isEqualTo("건강 이상 알림은 받을 수 있어요.");
    }
}
