package com.gaguraczi.paw.utils;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private static final DefaultRedisScript<Long> INCREMENT_WITH_TTL_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> COMPARE_AND_SET_SCRIPT = new DefaultRedisScript<>();
    private static final DefaultRedisScript<Long> COMPARE_AND_DELETE_SCRIPT = new DefaultRedisScript<>();

    static {
        INCREMENT_WITH_TTL_SCRIPT.setResultType(Long.class);
        INCREMENT_WITH_TTL_SCRIPT.setScriptText("""
                local count = redis.call('INCR', KEYS[1])
                if count == 1 then
                    redis.call('EXPIRE', KEYS[1], ARGV[1])
                end
                return count
                """);

        COMPARE_AND_SET_SCRIPT.setResultType(Long.class);
        COMPARE_AND_SET_SCRIPT.setScriptText("""
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    redis.call('SET', KEYS[1], ARGV[2], 'EX', ARGV[3])
                    return 1
                end
                return 0
                """);

        COMPARE_AND_DELETE_SCRIPT.setResultType(Long.class);
        COMPARE_AND_DELETE_SCRIPT.setScriptText("""
                if redis.call('GET', KEYS[1]) == ARGV[1] then
                    return redis.call('DEL', KEYS[1])
                end
                return 0
                """);
    }

    private final StringRedisTemplate redisTemplate;

    // 데이터 저장 (유효 시간 지정)
    public void setDataExpire(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, duration, TimeUnit.SECONDS);
    }

    // 키가 없을 때만 저장 (SET NX EX) — 성공 시 true
    public boolean setIfAbsent(String key, String value, long duration) {
        Boolean result = redisTemplate.opsForValue()
                .setIfAbsent(key, value, duration, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(result);
    }

    // 원자적 증가 — 최초 증가(값이 1)일 때만 TTL 설정 (INCR+EXPIRE를 Lua로 한 번에 실행)
    public long increment(String key, long duration) {
        Long count = redisTemplate.execute(
                INCREMENT_WITH_TTL_SCRIPT,
                List.of(key),
                String.valueOf(duration));
        return count == null ? 0L : count;
    }

    public long incrementBy(String key, long delta) {
        Long count = redisTemplate.opsForValue().increment(key, delta);
        return count == null ? 0L : count;
    }

    public List<String> multiGet(List<String> keys) {
        if (keys == null || keys.isEmpty()) {
            return List.of();
        }
        List<String> values = redisTemplate.opsForValue().multiGet(keys);
        return values == null ? List.of() : values;
    }

    public void addToSet(String key, String value) {
        redisTemplate.opsForSet().add(key, value);
    }

    public java.util.Set<String> members(String key) {
        java.util.Set<String> members = redisTemplate.opsForSet().members(key);
        return members == null ? java.util.Set.of() : members;
    }

    public void removeFromSet(String key, String value) {
        redisTemplate.opsForSet().remove(key, value);
    }

    // 데이터 조회
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 데이터 삭제
    public void deleteData(String key) {
        redisTemplate.delete(key);
    }

    // 현재 값이 expected와 같을 때만 newValue로 교체 (CAS SET EX) — 성공 시 true
    public boolean compareAndSet(String key, String expected, String newValue, long durationSeconds) {
        Long result = redisTemplate.execute(
                COMPARE_AND_SET_SCRIPT,
                List.of(key),
                expected,
                newValue,
                String.valueOf(durationSeconds));
        return result != null && result == 1L;
    }

    // 현재 값이 expected와 같을 때만 삭제 — 성공 시 true
    public boolean compareAndDelete(String key, String expected) {
        Long result = redisTemplate.execute(
                COMPARE_AND_DELETE_SCRIPT,
                List.of(key),
                expected);
        return result != null && result > 0L;
    }

    // 패턴에 맞는 키 일괄 삭제 (KEYS 대신 SCAN)
    public void deleteByPattern(String pattern) {
        ScanOptions options = ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        List<String> keysToDelete = redisTemplate.execute(connection -> {
            List<String> keys = new ArrayList<>();
            try (Cursor<byte[]> cursor = connection.keyCommands().scan(options)) {
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
