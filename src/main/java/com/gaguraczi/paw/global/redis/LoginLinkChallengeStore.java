package com.gaguraczi.paw.global.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.auth.enums.LinkChallengeType;
import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.utils.RedisUtil;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class LoginLinkChallengeStore {

    private static final String KEY_PREFIX = "auth:link:";
    private static final long TTL_SECONDS = 600L;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public String save(Pending pending) {
        String token = UUID.randomUUID().toString();
        try {
            redisUtil.setDataExpire(KEY_PREFIX + token, objectMapper.writeValueAsString(pending), TTL_SECONDS);
        } catch (JsonProcessingException e) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        return token;
    }

    public Pending get(String linkToken) {
        String json = redisUtil.getData(KEY_PREFIX + linkToken);
        if (json == null || json.isBlank()) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
        try {
            return objectMapper.readValue(json, Pending.class);
        } catch (JsonProcessingException e) {
            throw AuthException.of(AuthErrorCode.LOGIN_LINK_400);
        }
    }

    public void delete(String linkToken) {
        redisUtil.deleteData(KEY_PREFIX + linkToken);
    }

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Pending {
        private LinkChallengeType type;
        /** NEED_KAKAO_CONFIRM: 카카오 User uid / NEED_LOCAL_CONFIRM·MERGE: target(로컬) uid */
        private String uid;
        /** NEED_LOCAL_CONFIRM_MERGE: 병합 후 삭제할 카카오 전용 User */
        private String sourceUid;
        private String email;
        /** NEED_KAKAO_CONFIRM: 연동할 로컬 비밀번호 해시 */
        private String passwordHash;
        /** NEED_LOCAL_CONFIRM / MERGE: 연동할 카카오 providerId */
        private String kakaoProviderId;
        private String kakaoEmail;

        public static Pending needKakaoConfirm(String uid, String email, String passwordHash) {
            Pending p = new Pending();
            p.type = LinkChallengeType.NEED_KAKAO_CONFIRM;
            p.uid = uid;
            p.email = email;
            p.passwordHash = passwordHash;
            return p;
        }

        public static Pending needLocalConfirm(String uid, String email, String kakaoProviderId, String kakaoEmail) {
            Pending p = new Pending();
            p.type = LinkChallengeType.NEED_LOCAL_CONFIRM;
            p.uid = uid;
            p.email = email;
            p.kakaoProviderId = kakaoProviderId;
            p.kakaoEmail = kakaoEmail;
            return p;
        }

        public static Pending needLocalConfirmMerge(
                String sourceUid,
                String targetUid,
                String email,
                String kakaoProviderId
        ) {
            Pending p = new Pending();
            p.type = LinkChallengeType.NEED_LOCAL_CONFIRM_MERGE;
            p.sourceUid = sourceUid;
            p.uid = targetUid;
            p.email = email;
            p.kakaoProviderId = kakaoProviderId;
            return p;
        }
    }
}
