package com.gaguraczi.paw.domain.walk.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalkInProgressRedisStore {

    private static final String KEY_PREFIX = "walk:in-progress:";
    private static final long TTL_SECONDS = 6L * 60 * 60;

    private final RedisUtil redisUtil;
    private final ObjectMapper objectMapper;

    public boolean saveIfAbsent(WalkInProgressSession session) {
        try {
            return redisUtil.setIfAbsent(
                    key(session.getPetId()),
                    objectMapper.writeValueAsString(session),
                    TTL_SECONDS);
        } catch (JsonProcessingException e) {
            throw new GeneralException(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND, e);
        }
    }

    public Optional<WalkInProgressSession> getAndRefreshTtl(Long petId) {
        String json = redisUtil.getData(key(petId));
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        redisUtil.expire(key(petId), TTL_SECONDS);
        return Optional.of(parse(json));
    }

    public Optional<String> getRaw(Long petId) {
        String json = redisUtil.getData(key(petId));
        if (json == null || json.isBlank()) {
            return Optional.empty();
        }
        return Optional.of(json);
    }

    public WalkInProgressSession parse(String json) {
        try {
            return objectMapper.readValue(json, WalkInProgressSession.class);
        } catch (JsonProcessingException e) {
            log.warn("Failed to parse in-progress walk session", e);
            throw new GeneralException(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND, e);
        }
    }

    public Optional<String> markProcessingIfUnchanged(Long petId, String expectedJson) {
        WalkInProgressSession session = parse(expectedJson);
        if (session.isProcessing()) {
            return Optional.empty();
        }
        WalkInProgressSession processing = WalkInProgressSession.builder()
                .petId(session.getPetId())
                .startTime(session.getStartTime())
                .walkDate(session.getWalkDate())
                .processing(true)
                .build();
        try {
            String processingJson = objectMapper.writeValueAsString(processing);
            if (redisUtil.compareAndSet(key(petId), expectedJson, processingJson, TTL_SECONDS)) {
                return Optional.of(processingJson);
            }
            return Optional.empty();
        } catch (JsonProcessingException e) {
            throw new GeneralException(WalkErrorCode.WALK_IN_PROGRESS_NOT_FOUND, e);
        }
    }

    public boolean removeIfUnchanged(Long petId, String expectedJson) {
        return redisUtil.compareAndDelete(key(petId), expectedJson);
    }

    public boolean restoreIfUnchanged(Long petId, String expectedProcessingJson, String originalJson) {
        return redisUtil.compareAndSet(key(petId), expectedProcessingJson, originalJson, TTL_SECONDS);
    }

    private String key(Long petId) {
        return KEY_PREFIX + petId;
    }
}
