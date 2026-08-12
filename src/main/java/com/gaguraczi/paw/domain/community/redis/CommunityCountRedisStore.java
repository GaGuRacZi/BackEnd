package com.gaguraczi.paw.domain.community.redis;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.utils.RedisUtil;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CommunityCountRedisStore {

    private static final String VIEW_KEY = "community:view:";
    private static final String LIKE_KEY = "community:like:";
    private static final String LOCK_KEY = "community:count:lock:";
    private static final String DEDUP_KEY = "community:view:dedup:";
    private static final String DIRTY_SET = "community:count:dirty";

    private static final long COUNT_TTL_SECONDS = 7L * 24 * 60 * 60;
    private static final long LOCK_TTL_SECONDS = 30L;
    private static final long DEDUP_TTL_SECONDS = 10L * 60;

    private final RedisUtil redisUtil;
    private final CommunityRepository communityRepository;
    private final PlatformTransactionManager transactionManager;

    private TransactionTemplate requiresNewTemplate;

    @PostConstruct
    void initRequiresNewTemplate() {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.requiresNewTemplate = template;
    }

    public long getViewCount(Community community) {
        return getOrWarm(VIEW_KEY + community.getPostId(), community.getViewCount());
    }

    public long getLikeCount(Community community) {
        return getOrWarm(LIKE_KEY + community.getPostId(), community.getLikeCount());
    }

    public Map<Long, Long> getViewCounts(List<Community> communities) {
        return multiGetCounts(communities, VIEW_KEY, true);
    }

    public Map<Long, Long> getLikeCounts(List<Community> communities) {
        return multiGetCounts(communities, LIKE_KEY, false);
    }

    public long increaseView(Community community, UUID viewerUid) {
        Long postId = community.getPostId();
        if (viewerUid != null) {
            String dedupKey = DEDUP_KEY + postId + ":" + viewerUid;
            if (!redisUtil.setIfAbsent(dedupKey, "1", DEDUP_TTL_SECONDS)) {
                return getViewCount(community);
            }
        }
        warmIfAbsent(VIEW_KEY + postId, community.getViewCount());
        long next = redisUtil.increment(VIEW_KEY + postId, COUNT_TTL_SECONDS);
        markDirty(postId);
        return next;
    }

    public long increaseLike(Community community) {
        warmIfAbsent(LIKE_KEY + community.getPostId(), community.getLikeCount());
        long next = redisUtil.increment(LIKE_KEY + community.getPostId(), COUNT_TTL_SECONDS);
        markDirty(community.getPostId());
        return Math.max(0L, next);
    }

    public long decreaseLike(Community community) {
        warmIfAbsent(LIKE_KEY + community.getPostId(), community.getLikeCount());
        long next = redisUtil.decrementClampToZero(LIKE_KEY + community.getPostId(), COUNT_TTL_SECONDS);
        markDirty(community.getPostId());
        return next;
    }

    public void deleteCounts(Long postId) {
        redisUtil.deleteData(VIEW_KEY + postId);
        redisUtil.deleteData(LIKE_KEY + postId);
        redisUtil.deleteData(LOCK_KEY + postId);
        redisUtil.deleteByPattern(DEDUP_KEY + postId + ":*");
        redisUtil.removeFromSet(DIRTY_SET, String.valueOf(postId));
    }

    public void flushAllDirty() {
        for (String postIdRaw : redisUtil.members(DIRTY_SET)) {
            try {
                Long postId = Long.parseLong(postIdRaw);
                requiresNewTemplate.executeWithoutResult(status -> doFlushPost(postId));
            } catch (NumberFormatException e) {
                redisUtil.removeFromSet(DIRTY_SET, postIdRaw);
            }
        }
    }

    @Transactional
    public void flushPost(Long postId) {
        doFlushPost(postId);
    }

    private void doFlushPost(Long postId) {
        String lockKey = LOCK_KEY + postId;
        String token = UUID.randomUUID().toString();
        if (!redisUtil.setIfAbsent(lockKey, token, LOCK_TTL_SECONDS)) {
            return;
        }
        try {
            Optional<Community> optional = communityRepository.findById(postId);
            if (optional.isEmpty()) {
                deleteCounts(postId);
                return;
            }
            Community community = optional.get();
            String viewKey = VIEW_KEY + postId;
            String likeKey = LIKE_KEY + postId;
            String viewRaw = redisUtil.getData(viewKey);
            String likeRaw = redisUtil.getData(likeKey);
            long view = parseOrDefault(viewRaw, community.getViewCount());
            long like = parseOrDefault(likeRaw, community.getLikeCount());
            community.syncCounts(view, like);
            communityRepository.save(community);
            redisUtil.removeFromSetIfCountersUnchanged(
                    viewKey,
                    likeKey,
                    DIRTY_SET,
                    String.valueOf(view),
                    String.valueOf(like),
                    String.valueOf(postId)
            );
        } finally {
            redisUtil.compareAndDelete(lockKey, token);
        }
    }

    private long getOrWarm(String key, long dbValue) {
        String cached = redisUtil.getData(key);
        if (cached != null) {
            return parseOrDefault(cached, dbValue);
        }
        warmIfAbsent(key, dbValue);
        return dbValue;
    }

    private void warmIfAbsent(String key, long dbValue) {
        redisUtil.setIfAbsent(key, String.valueOf(dbValue), COUNT_TTL_SECONDS);
    }

    private void markDirty(Long postId) {
        redisUtil.addToSet(DIRTY_SET, String.valueOf(postId));
    }

    private Map<Long, Long> multiGetCounts(List<Community> communities, String prefix, boolean view) {
        Map<Long, Long> result = new HashMap<>();
        if (communities == null || communities.isEmpty()) {
            return result;
        }
        List<String> keys = new ArrayList<>(communities.size());
        for (Community community : communities) {
            keys.add(prefix + community.getPostId());
        }
        List<String> values = redisUtil.multiGet(keys);
        for (int i = 0; i < communities.size(); i++) {
            Community community = communities.get(i);
            long dbFallback = view ? community.getViewCount() : community.getLikeCount();
            String value = i < values.size() ? values.get(i) : null;
            if (value == null) {
                warmIfAbsent(prefix + community.getPostId(), dbFallback);
                result.put(community.getPostId(), dbFallback);
            } else {
                result.put(community.getPostId(), parseOrDefault(value, dbFallback));
            }
        }
        return result;
    }

    private long parseOrDefault(String raw, long fallback) {
        if (raw == null || raw.isBlank()) {
            return fallback;
        }
        try {
            return Long.parseLong(raw);
        } catch (NumberFormatException e) {
            return fallback;
        }
    }
}
