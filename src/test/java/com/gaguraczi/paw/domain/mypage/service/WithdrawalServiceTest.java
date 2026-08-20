package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.global.time.AppTime;
import com.gaguraczi.paw.utils.S3.S3Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WithdrawalServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private CommunityRepository communityRepository;
    @Mock
    private RefreshTokenRedisStore refreshTokenRedisStore;
    @Mock
    private S3Utils s3Utils;
    @Mock
    private Clock clock;

    @InjectMocks
    private WithdrawalService withdrawalService;

    @Test
    void 탈퇴하면_식별정보와_위치를_지우고_리프레시토큰과_프로필을_정리한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).profileS3Key("user/a.png").locationAddress("서울").build();
        when(securityUtils.currentUser()).thenReturn(user);
        when(clock.getZone()).thenReturn(AppTime.KST);
        when(clock.instant()).thenReturn(Instant.parse("2026-08-20T20:00:00Z"));

        withdrawalService.withdraw();

        assertThat(user.isDeleted()).isTrue();
        assertThat(user.getUserPoint()).isNull();
        assertThat(user.getRegion()).isNull();
        assertThat(user.getLocationAddress()).isNull();
        assertThat(user.getProfileS3Key()).isNull();
        verify(refreshTokenRedisStore).deleteAll(uid.toString());
        verify(s3Utils).scheduleDeleteAfterCommit("user/a.png");
    }
}
