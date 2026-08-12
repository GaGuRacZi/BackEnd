package com.gaguraczi.paw.domain.community.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityCountFlushScheduler {

    private final CommunityCountRedisStore communityCountRedisStore;

    @Scheduled(fixedDelay = 60_000)
    public void flushDirtyCounts() {
        try {
            communityCountRedisStore.flushAllDirty();
        } catch (Exception e) {
            log.warn("Community count flush failed", e);
        }
    }
}
