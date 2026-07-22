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

    /**
     * 저장된 토큰이 expected와 일치할 때만 newToken으로 교체.
     * @return 교체 성공 여부
     */
    public boolean rotate(String uid, String provider, String expected, String newToken, Duration ttl) {
        return redisUtil.compareAndSet(key(uid, provider), expected, newToken, ttl.toSeconds());
    }

    public void delete(String uid, String provider) {
        redisUtil.deleteData(key(uid, provider));
    }

    /**
     * 저장된 토큰이 expected와 일치할 때만 삭제.
     * @return 삭제 성공 여부
     */
    public boolean delete(String uid, String provider, String expected) {
        return redisUtil.compareAndDelete(key(uid, provider), expected);
    }

    // 특정 유저의 모든 소셜 토큰 삭제 (전체 로그아웃·회원탈퇴)
    public void deleteAll(String uid) {
        redisUtil.deleteByPattern(KEY_PREFIX + uid + ":*");
    }

    private static String key(String uid, String provider) {
        return KEY_PREFIX + uid + ":" + provider;
    }
}
