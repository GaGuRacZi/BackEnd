package com.gaguraczi.paw.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final StringRedisTemplate redisTemplate;

    // 데이터 저장 (유효 시간 지정)
    public void setDataExpire(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
    }

    // 데이터 조회
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    // 패턴에 맞는 키 일괄 삭제 (KEYS 대신 SCAN)
    public void deleteByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        List<String> keysToDelete = redisTemplate.execute(connection -> {
            List<String> keys = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.scan(options)) {
                cursor.forEachRemaining(
                        key -> keys.add(new String(key, StandardCharsets.UTF_8)));
            }
            return keys;
        }, true);

        if (keysToDelete != null && !keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }
    }
}
