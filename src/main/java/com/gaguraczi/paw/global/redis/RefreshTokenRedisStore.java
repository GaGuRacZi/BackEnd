/*
package com.gaguraczi.paw.global.redis;

import com.gaguraczi.paw.domain.user.entity.RefreshToken;
import com.gaguraczi.paw.domain.user.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Redis 리프레시 토큰 저장소.
// 키 형식: token_redis:{uid}:{provider}
@Component
@RequiredArgsConstructor
public class RefreshTokenRedisStore {

    private static final String KEY_PREFIX = "token_redis:";

    private final RefreshTokenRepository refreshTokenRepository;
    private final StringRedisTemplate stringRedisTemplate;

    public void save(String uid, String provider, String token, Duration ttl) {
        refreshTokenRepository.save(
                RefreshToken.create(uid, provider, token, ttl.toSeconds()));
    }

    public Optional<String> find(String uid, String provider) {
        return refreshTokenRepository.findById(uid + ":" + provider)
                .map(RefreshToken::getRefreshToken);
    }

    public void delete(String uid, String provider) {
        refreshTokenRepository.deleteById(uid + ":" + provider);
    }

    // 특정 유저의 모든 소셜 토큰 삭제 (전체 로그아웃·회원탈퇴)
    // KEYS 대신 SCAN을 사용해 Redis 블로킹 방지
    public void deleteAll(String uid) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(KEY_PREFIX + uid + ":*")
                .count(100)
                .build();

        List<String> keysToDelete = stringRedisTemplate.execute(connection -> {
            List<String> keys = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                cursor.forEachRemaining(
                        key -> keys.add(new String(key, StandardCharsets.UTF_8)));
            }
            return keys;
        }, true);

        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            stringRedisTemplate.delete(keysToDelete);
        }
    }
}
*/