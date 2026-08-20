package com.gaguraczi.paw.domain.auth.service;

import com.gaguraczi.paw.domain.auth.client.KakaoApiClient;
import com.gaguraczi.paw.domain.auth.dto.req.LocalLoginReq;
import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.auth.repository.OAuthRepository;
import com.gaguraczi.paw.domain.location.service.NaverMapService;
import com.gaguraczi.paw.domain.mypage.service.NotificationSettingService;
import com.gaguraczi.paw.domain.region.service.LegalRegionService;
import com.gaguraczi.paw.domain.terms.service.TermsService;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.redis.LoginLinkChallengeStore;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.JwtTokenProvider;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceWithdrawnUserTest {

    @Mock private UserRepository userRepository;
    @Mock private OAuthRepository oAuthRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private JwtTokenProvider jwtTokenProvider;
    @Mock private RefreshTokenRedisStore refreshTokenRedisStore;
    @Mock private LoginLinkChallengeStore loginLinkChallengeStore;
    @Mock private KakaoApiClient kakaoApiClient;
    @Mock private SecurityUtils securityUtils;
    @Mock private EmailVerificationService emailVerificationService;
    @Mock private LegalRegionService legalRegionService;
    @Mock private NaverMapService naverMapService;
    @Mock private TermsService termsService;
    @Mock private NotificationSettingService notificationSettingService;
    @Mock private ObjectProvider<AuthService> self;
    @Mock private RedisUtil redisUtil;

    @InjectMocks
    private AuthService authService;

    @Test
    void 탈퇴_계정은_로컬_로그인_토큰을_발급하지_않는다() {
        User user = User.builder().uid(UUID.randomUUID()).email("user@example.com").build();
        user.withdraw();
        OAuth oauth = OAuth.builder()
                .user(user)
                .providerId("user@example.com")
                .providerType(SocialType.LOCAL)
                .password("hashed")
                .email("user@example.com")
                .build();
        when(oAuthRepository.findByEmailAndProviderType("user@example.com", SocialType.LOCAL))
                .thenReturn(Optional.of(oauth));
        when(passwordEncoder.matches("pw", "hashed")).thenReturn(true);

        LocalLoginReq req = new LocalLoginReq();
        ReflectionTestUtils.setField(req, "email", "user@example.com");
        ReflectionTestUtils.setField(req, "password", "pw");

        assertThatThrownBy(() -> authService.loginLocal(req))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.LOCAL_LOGIN_401_2);
    }

    @Test
    void 탈퇴_계정은_카카오_로그인_토큰을_발급하지_않는다() {
        User user = User.builder().uid(UUID.randomUUID()).build();
        user.withdraw();
        OAuth oauth = OAuth.builder()
                .user(user)
                .providerId("kakao-1")
                .providerType(SocialType.KAKAO)
                .build();
        when(oAuthRepository.findByProviderIdAndProviderType("kakao-1", SocialType.KAKAO))
                .thenReturn(Optional.of(oauth));

        assertThatThrownBy(() -> authService.completeLoginKakao(new KakaoApiClient.KakaoUserInfo("kakao-1", null)))
                .isInstanceOf(AuthException.class)
                .extracting(e -> ((AuthException) e).getCode())
                .isEqualTo(AuthErrorCode.LOCAL_LOGIN_401_2);
    }
}
