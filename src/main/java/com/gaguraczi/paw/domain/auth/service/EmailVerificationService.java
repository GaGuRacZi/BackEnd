package com.gaguraczi.paw.domain.auth.service;

import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.auth.repository.OAuthRepository;
import com.gaguraczi.paw.domain.auth.enums.SocialType;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.config.properties.SmtpProperties;
import com.gaguraczi.paw.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailVerificationService {

    private static final String CODE_KEY = "auth:email:code:";
    private static final String COOLDOWN_KEY = "auth:email:cooldown:";
    private static final String VERIFIED_KEY = "auth:email:verified:";
    private static final String ATTEMPT_KEY = "auth:email:attempt:";

    private static final long CODE_TTL_SECONDS = 300L;
    private static final long COOLDOWN_TTL_SECONDS = 60L;
    private static final long VERIFIED_TTL_SECONDS = 1800L;
    private static final long MAX_ATTEMPTS = 5L;

    private final JavaMailSender mailSender;
    private final SmtpProperties smtpProperties;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository;
    private final OAuthRepository oAuthRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public void sendCode(String email) {
        String normalized = normalize(email);

        if (userRepository.existsByEmail(normalized)
                || oAuthRepository.existsByEmailAndProviderType(normalized, SocialType.LOCAL)) {
            throw AuthException.of(AuthErrorCode.LOCAL_SIGNUP_409_1);
        }

        if (!redisUtil.setIfAbsent(COOLDOWN_KEY + normalized, "1", COOLDOWN_TTL_SECONDS)) {
            throw AuthException.of(AuthErrorCode.EMAIL_SEND_429);
        }

        String code = String.format("%06d", secureRandom.nextInt(1_000_000));
        redisUtil.setDataExpire(CODE_KEY + normalized, code, CODE_TTL_SECONDS);

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(smtpProperties.getFromEmail());
            message.setTo(normalized);
            message.setSubject("[Paw] 이메일 인증번호");
            message.setText("인증번호는 [" + code + "] 입니다.\n5분 이내에 입력해 주세요.");
            mailSender.send(message);
        } catch (Exception e) {
            log.error("이메일 전송 실패: {}", e.getMessage());
            redisUtil.deleteData(CODE_KEY + normalized);
            redisUtil.deleteData(COOLDOWN_KEY + normalized);
            throw AuthException.of(AuthErrorCode.EMAIL_SEND_400);
        }
    }

    public void verifyCode(String email, String code) {
        String normalized = normalize(email);
        String attemptKey = ATTEMPT_KEY + normalized;
        long attempts = redisUtil.increment(attemptKey, CODE_TTL_SECONDS);
        if (attempts > MAX_ATTEMPTS) {
            throw AuthException.of(AuthErrorCode.EMAIL_CODE_INVALID);
        }

        String stored = redisUtil.getData(CODE_KEY + normalized);
        if (stored == null || !stored.equals(code)) {
            throw AuthException.of(AuthErrorCode.EMAIL_CODE_INVALID);
        }

        redisUtil.deleteData(attemptKey);
        redisUtil.deleteData(CODE_KEY + normalized);
        redisUtil.setDataExpire(VERIFIED_KEY + normalized, "1", VERIFIED_TTL_SECONDS);
    }

    public void requireVerified(String email) {
        String normalized = normalize(email);
        if (redisUtil.getData(VERIFIED_KEY + normalized) == null) {
            throw AuthException.of(AuthErrorCode.EMAIL_NOT_VERIFIED);
        }
    }

    public void consumeVerified(String email) {
        redisUtil.deleteData(VERIFIED_KEY + normalize(email));
    }

    private String normalize(String email) {
        return email.trim().toLowerCase();
    }
}
