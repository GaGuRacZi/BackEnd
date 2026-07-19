package com.gaguraczi.paw.global.redis;

import com.gaguraczi.paw.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;

// Redis 리프레시 토큰 저장소.
// 키 형식: token_redis:{uid}:{provider}
@Component
@RequiredArgsConstructor
public class RefreshTokenRedisStore {

    private static final String KEY_PREFIX = "token_redis:";

    private final RedisUtil redisUtil;

    public void save(String uid, String provider, String token, Duration ttl) {
        redisUtil.setDataExpire(key(uid, provider), token, ttl.toSeconds());
    }

    public Optional<String> find(String uid, String provider) {
        String value = redisUtil.getData(key(uid, provider));
        return Optional.ofNullable(value).filter(v -> !v.isBlank());
    }

    public void delete(String uid, String provider) {
        redisUtil.deleteData(key(uid, provider));
    }

    // 특정 유저의 모든 소셜 토큰 삭제 (전체 로그아웃·회원탈퇴)
    public void deleteAll(String uid) {
        redisUtil.deleteByPattern(KEY_PREFIX + uid + ":*");
    }

    private static String key(String uid, String provider) {
        return KEY_PREFIX + uid + ":" + provider;
    }
}
