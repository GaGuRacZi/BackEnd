package com.gaguraczi.paw.domain.auth.service;

import com.gaguraczi.paw.domain.auth.client.KakaoApiClient;
import com.gaguraczi.paw.domain.auth.dto.req.KakaoLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmKakaoReq;
import com.gaguraczi.paw.domain.auth.dto.req.LinkConfirmLocalReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalLoginReq;
import com.gaguraczi.paw.domain.auth.dto.req.LocalSignupReq;
import com.gaguraczi.paw.domain.auth.dto.req.OnboardingProfileReq;
import com.gaguraczi.paw.domain.auth.dto.req.RefreshTokenReq;
import com.gaguraczi.paw.domain.auth.dto.res.LoginLinkChallengeRes;
import com.gaguraczi.paw.domain.auth.dto.res.LoginRes;
import com.gaguraczi.paw.domain.auth.entity.OAuth;
import com.gaguraczi.paw.domain.auth.enums.LinkChallengeType;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.auth.exception.code.AuthSuccessCode;
import com.gaguraczi.paw.domain.auth.repository.OAuthRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import com.gaguraczi.paw.global.redis.LoginLinkChallengeStore;
import com.gaguraczi.paw.global.redis.RefreshTokenRedisStore;
import com.gaguraczi.paw.global.security.JwtTokenProvider;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenRedisStore refreshTokenRedisStore;
    private final LoginLinkChallengeStore loginLinkChallengeStore;
    private final KakaoApiClient kakaoApiClient;
    private final SecurityUtils securityUtils;
    private final EmailVerificationService emailVerificationService;
    private final ObjectProvider<AuthService> self;

    public record AuthResult(Object result, BaseSuccessCode successCode) {
    }

    @Transactional
    public AuthResult signupLocal(LocalSignupReq req) {
        emailVerificationService.requireVerified(req.getEmail());

        if (oAuthRepository.existsByEmailAndProviderType(req.getEmail(), SocialType.LOCAL)) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        Optional<User> existingUser = userRepository.findByEmail(req.getEmail());
        if (existingUser.isPresent()) {
            User user = existingUser.get();
            if (oAuthRepository.existsByUserAndProviderType(user, SocialType.KAKAO)
                    && !oAuthRepository.existsByUserAndProviderType(user, SocialType.LOCAL)) {
                return createKakaoConfirmChallenge(user, req.getEmail(), req.getPassword());
            }
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        User user;
        try {
            user = userRepository.saveAndFlush(User.builder()
                    .email(req.getEmail().trim().toLowerCase())
                    .isNew(true)
                    .build());
        } catch (DataIntegrityViolationException e) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        linkLocalOAuth(user, req.getEmail().trim().toLowerCase(), passwordEncoder.encode(req.getPassword()));
        emailVerificationService.consumeVerified(req.getEmail());

        LoginRes loginRes = issueTokens(user, SocialType.LOCAL.name());
        return new AuthResult(loginRes, AuthSuccessCode.LOCAL_SIGNUP_200_1);
    }

    public AuthResult sendEmailCode(String email) {
        emailVerificationService.sendCode(email);
        return new AuthResult(null, AuthSuccessCode.EMAIL_SEND_200);
    }

    public AuthResult verifyEmailCode(String email, String code) {
        emailVerificationService.verifyCode(email, code);
        return new AuthResult(null, AuthSuccessCode.EMAIL_VERIFY_200);
    }

    @Transactional
    public AuthResult loginLocal(LocalLoginReq req) {
        Optional<OAuth> localOAuth = oAuthRepository.findByEmailAndProviderType(req.getEmail(), SocialType.LOCAL);

        if (localOAuth.isEmpty()) {
            Optional<User> userOpt = userRepository.findByEmail(req.getEmail());
            if (userOpt.isPresent()
                    && oAuthRepository.existsByUserAndProviderType(userOpt.get(), SocialType.KAKAO)
                    && !oAuthRepository.existsByUserAndProviderType(userOpt.get(), SocialType.LOCAL)) {
                return createKakaoConfirmChallenge(userOpt.get(), req.getEmail(), req.getPassword());
            }
            throw AuthException.of(AuthErrorCode.LOCAL_LOGIN_401_2);
        }

        OAuth oauth = localOAuth.get();
        if (oauth.getPassword() == null
                || !passwordEncoder.matches(req.getPassword(), oauth.getPassword())) {
            throw AuthException.of(AuthErrorCode.LOCAL_LOGIN_401_2);
        }

        User user = oauth.getUser();
        LoginRes loginRes = issueTokens(user, SocialType.LOCAL.name());
        BaseSuccessCode code = user.isNew()
                ? AuthSuccessCode.LOCAL_LOGIN_200_1
                : AuthSuccessCode.LOCAL_LOGIN_200_2;
        return new AuthResult(loginRes, code);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuthResult loginKakao(KakaoLoginReq req) {
        KakaoApiClient.KakaoUserInfo kakaoUser = kakaoApiClient.getUserInfo(req.getAccessToken());
        return self.getObject().completeLoginKakao(kakaoUser);
    }

    @Transactional
    public AuthResult completeLoginKakao(KakaoApiClient.KakaoUserInfo kakaoUser) {
        Optional<OAuth> existingKakao =
                oAuthRepository.findByProviderIdAndProviderType(kakaoUser.providerId(), SocialType.KAKAO);
        if (existingKakao.isPresent()) {
            User user = existingKakao.get().getUser();
            LoginRes loginRes = issueTokens(user, SocialType.KAKAO.name());
            BaseSuccessCode code = user.isNew()
                    ? AuthSuccessCode.KAKAO_LOGIN_200_1
                    : AuthSuccessCode.KAKAO_LOGIN_200_2;
            return new AuthResult(loginRes, code);
        }

        // 카카오 이메일이 기존 로컬 계정과 같으면 → 연동 창 (로컬 비밀번호 확인 후 KAKAO 연결)
        if (kakaoUser.email() != null && !kakaoUser.email().isBlank()) {
            Optional<User> existingUser = userRepository.findByEmail(kakaoUser.email());
            if (existingUser.isPresent()) {
                User user = existingUser.get();
                if (oAuthRepository.existsByUserAndProviderType(user, SocialType.KAKAO)) {
                    throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
                }
                if (oAuthRepository.existsByUserAndProviderType(user, SocialType.LOCAL)) {
                    return createLocalConfirmChallenge(user, kakaoUser);
                }
                throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
            }
        }

        User user = userRepository.save(User.builder()
                .email(blankToNull(kakaoUser.email()))
                .isNew(true)
                .build());
        linkKakaoOAuth(user, kakaoUser.providerId(), blankToNull(kakaoUser.email()));

        LoginRes loginRes = issueTokens(user, SocialType.KAKAO.name());
        return new AuthResult(loginRes, AuthSuccessCode.KAKAO_LOGIN_200_1);
    }

    /**
     * 카카오 온보딩: 이름, 닉네임, 한줄소개 등록 후 isNew=false
     */
    @Transactional
    public AuthResult completeKakaoOnboarding(OnboardingProfileReq req) {
        User current = securityUtils.currentUser();

        if (!current.isNew()) {
            throw AuthException.of(AuthErrorCode.ONBOARDING_400);
        }

        current.completeOnboarding(req.getName(), req.getNickname(), req.getIntro());
        return new AuthResult(null, AuthSuccessCode.ONBOAREDING_200);
    }

    /**
     * 로그인된 사용자가 카카오 계정을 현재 User에 연동.
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuthResult linkKakao(KakaoLoginReq req) {
        KakaoApiClient.KakaoUserInfo kakaoUser = kakaoApiClient.getUserInfo(req.getAccessToken());
        return self.getObject().completeLinkKakao(kakaoUser);
    }

    @Transactional
    public AuthResult completeLinkKakao(KakaoApiClient.KakaoUserInfo kakaoUser) {
        User current = securityUtils.currentUser();

        if (oAuthRepository.existsByUserAndProviderType(current, SocialType.KAKAO)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }
        if (oAuthRepository.existsByProviderIdAndProviderType(kakaoUser.providerId(), SocialType.KAKAO)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }

        linkKakaoOAuth(current, kakaoUser.providerId(), blankToNull(kakaoUser.email()));
        LoginRes loginRes = issueTokens(current, SocialType.KAKAO.name());
        return new AuthResult(loginRes, AuthSuccessCode.LOGIN_LINK_200);
    }

    /**
     * 로컬 시도 후 연동 창에서 카카오로 확인 → LOCAL OAuth 추가
     */
    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public AuthResult confirmLinkWithKakao(LinkConfirmKakaoReq req) {
        LoginLinkChallengeStore.Pending pending = loginLinkChallengeStore.get(req.getLinkToken());
        if (pending.getType() != LinkChallengeType.NEED_KAKAO_CONFIRM) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }

        KakaoApiClient.KakaoUserInfo kakaoUser = kakaoApiClient.getUserInfo(req.getAccessToken());
        return self.getObject().completeConfirmLinkWithKakao(req, pending, kakaoUser);
    }

    @Transactional
    public AuthResult completeConfirmLinkWithKakao(
            LinkConfirmKakaoReq req,
            LoginLinkChallengeStore.Pending pending,
            KakaoApiClient.KakaoUserInfo kakaoUser
    ) {
        User user = userRepository.findById(UUID.fromString(pending.getUid()))
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));

        OAuth kakaoOAuth = oAuthRepository
                .findByProviderIdAndProviderType(kakaoUser.providerId(), SocialType.KAKAO)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        if (!kakaoOAuth.getUser().getUid().equals(user.getUid())) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }

        if (oAuthRepository.existsByUserAndProviderType(user, SocialType.LOCAL)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }

        linkLocalOAuth(user, pending.getEmail(), pending.getPasswordHash());
        loginLinkChallengeStore.delete(req.getLinkToken());

        LoginRes loginRes = issueTokens(user, SocialType.LOCAL.name());
        return new AuthResult(loginRes, AuthSuccessCode.LOGIN_LINK_200);
    }

    /**
     * 로컬 비밀번호 확인 후 카카오 연동
     * - NEED_LOCAL_CONFIRM: 카카오 로그인 시 (아직 KAKAO OAuth 없음) → 로컬 User에 KAKAO 추가
     * - NEED_LOCAL_CONFIRM_MERGE: 온보딩 시 → 카카오 User를 로컬 User로 병합
     */
    @Transactional
    public AuthResult confirmLinkWithLocal(LinkConfirmLocalReq req) {
        LoginLinkChallengeStore.Pending pending = loginLinkChallengeStore.get(req.getLinkToken());

        if (pending.getType() == LinkChallengeType.NEED_LOCAL_CONFIRM) {
            return confirmLocalAttachKakao(req, pending);
        }
        if (pending.getType() == LinkChallengeType.NEED_LOCAL_CONFIRM_MERGE) {
            return confirmLocalMergeKakao(req, pending);
        }
        throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
    }

    private AuthResult confirmLocalAttachKakao(
            LinkConfirmLocalReq req,
            LoginLinkChallengeStore.Pending pending
    ) {
        User user = userRepository.findById(UUID.fromString(pending.getUid()))
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));

        OAuth localOAuth = oAuthRepository.findByEmailAndProviderType(pending.getEmail(), SocialType.LOCAL)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        if (!localOAuth.getUser().getUid().equals(user.getUid())) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        if (localOAuth.getPassword() == null
                || !passwordEncoder.matches(req.getPassword(), localOAuth.getPassword())) {
            throw AuthException.of(AuthErrorCode.LOCAL_LOGIN_401_2);
        }

        if (oAuthRepository.existsByUserAndProviderType(user, SocialType.KAKAO)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }
        if (oAuthRepository.existsByProviderIdAndProviderType(pending.getKakaoProviderId(), SocialType.KAKAO)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }

        linkKakaoOAuth(user, pending.getKakaoProviderId(), pending.getKakaoEmail());
        loginLinkChallengeStore.delete(req.getLinkToken());

        LoginRes loginRes = issueTokens(user, SocialType.KAKAO.name());
        return new AuthResult(loginRes, AuthSuccessCode.LOGIN_LINK_200);
    }

    private AuthResult confirmLocalMergeKakao(
            LinkConfirmLocalReq req,
            LoginLinkChallengeStore.Pending pending
    ) {
        User target = userRepository.findById(UUID.fromString(pending.getUid()))
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        User source = userRepository.findById(UUID.fromString(pending.getSourceUid()))
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));

        OAuth localOAuth = oAuthRepository.findByEmailAndProviderType(pending.getEmail(), SocialType.LOCAL)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        if (!localOAuth.getUser().getUid().equals(target.getUid())) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        if (localOAuth.getPassword() == null
                || !passwordEncoder.matches(req.getPassword(), localOAuth.getPassword())) {
            throw AuthException.of(AuthErrorCode.LOCAL_LOGIN_401_2);
        }

        if (oAuthRepository.existsByUserAndProviderType(target, SocialType.KAKAO)) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400_3);
        }

        OAuth sourceKakao = oAuthRepository
                .findByProviderIdAndProviderType(pending.getKakaoProviderId(), SocialType.KAKAO)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.LOGIN_LINK_400));
        if (!sourceKakao.getUser().getUid().equals(source.getUid())) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }

        oAuthRepository.delete(sourceKakao);
        oAuthRepository.flush();
        linkKakaoOAuth(target, pending.getKakaoProviderId(), blankToNull(pending.getEmail()));

        List<OAuth> remaining = oAuthRepository.findAllByUser(source);
        oAuthRepository.deleteAll(remaining);
        refreshTokenRedisStore.deleteAll(source.getUid().toString());
        userRepository.delete(source);

        loginLinkChallengeStore.delete(req.getLinkToken());

        LoginRes loginRes = issueTokens(target, SocialType.KAKAO.name());
        return new AuthResult(loginRes, AuthSuccessCode.LOGIN_LINK_200);
    }

    @Transactional
    public AuthResult reissue(RefreshTokenReq req) {
        String refreshToken = req.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)
                || !JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.parseTokenType(refreshToken))) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        String uid = jwtTokenProvider.parseUid(refreshToken);
        String provider = jwtTokenProvider.parseProvider(refreshToken);
        if (provider == null) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        String stored = refreshTokenRedisStore.find(uid, provider)
                .orElseThrow(() -> AuthException.of(AuthErrorCode.REFRESH_INVALID));
        if (!stored.equals(refreshToken)) {
            throw AuthException.of(AuthErrorCode.REFRESH_INVALID);
        }

        User user = userRepository.findById(UUID.fromString(uid))
                .orElseThrow(() -> AuthException.of(AuthErrorCode.REFRESH_INVALID));

        LoginRes loginRes = issueTokens(user, provider);
        return new AuthResult(loginRes, AuthSuccessCode.REFRESH_200);
    }

    @Transactional
    public void logout(RefreshTokenReq req) {
        String refreshToken = req.getRefreshToken();
        if (!jwtTokenProvider.validateToken(refreshToken)
                || !JwtTokenProvider.TOKEN_TYPE_REFRESH.equals(jwtTokenProvider.parseTokenType(refreshToken))) {
            throw AuthException.of(AuthErrorCode.LOGOUT_INVALID);
        }

        String uid = jwtTokenProvider.parseUid(refreshToken);
        String provider = jwtTokenProvider.parseProvider(refreshToken);
        if (provider == null) {
            throw AuthException.of(AuthErrorCode.LOGOUT_INVALID);
        }

        String stored = refreshTokenRedisStore.find(uid, provider).orElse(null);
        if (stored == null || !stored.equals(refreshToken)) {
            throw AuthException.of(AuthErrorCode.LOGOUT_INVALID);
        }

        refreshTokenRedisStore.delete(uid, provider);
    }

    private AuthResult createKakaoConfirmChallenge(User user, String email, String rawPassword) {
        String linkToken = loginLinkChallengeStore.save(
                LoginLinkChallengeStore.Pending.needKakaoConfirm(
                        user.getUid().toString(),
                        email,
                        passwordEncoder.encode(rawPassword)
                )
        );
        LoginLinkChallengeRes challenge = LoginLinkChallengeRes.builder()
                .linkToken(linkToken)
                .existingProvider(SocialType.KAKAO.name())
                .email(email)
                .build();
        return new AuthResult(challenge, AuthSuccessCode.LOGIN_LINK_201);
    }

    private AuthResult createLocalConfirmChallenge(User user, KakaoApiClient.KakaoUserInfo kakaoUser) {
        String linkToken = loginLinkChallengeStore.save(
                LoginLinkChallengeStore.Pending.needLocalConfirm(
                        user.getUid().toString(),
                        kakaoUser.email(),
                        kakaoUser.providerId(),
                        kakaoUser.email()
                )
        );
        LoginLinkChallengeRes challenge = LoginLinkChallengeRes.builder()
                .linkToken(linkToken)
                .existingProvider(SocialType.LOCAL.name())
                .email(kakaoUser.email())
                .build();
        return new AuthResult(challenge, AuthSuccessCode.LOGIN_LINK_201);
    }

    private void linkLocalOAuth(User user, String email, String encodedPassword) {
        oAuthRepository.save(OAuth.builder()
                .user(user)
                .providerId(email)
                .providerType(SocialType.LOCAL)
                .password(encodedPassword)
                .email(email)
                .build());
    }

    private void linkKakaoOAuth(User user, String providerId, String email) {
        oAuthRepository.save(OAuth.builder()
                .user(user)
                .providerId(providerId)
                .providerType(SocialType.KAKAO)
                .email(email)
                .build());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value;
    }

    private LoginRes issueTokens(User user, String provider) {
        String uid = user.getUid().toString();
        String accessToken = jwtTokenProvider.createAccessToken(uid);
        String refreshToken = jwtTokenProvider.createRefreshToken(uid, provider);

        refreshTokenRedisStore.save(
                uid,
                provider,
                refreshToken,
                Duration.ofMillis(jwtTokenProvider.getRefreshExpMs())
        );

        return LoginRes.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .isNew(user.isNew())
                .uid(user.getUid())
                .build();
    }
}
