package com.gaguraczi.paw.domain.community.service;

import com.gaguraczi.paw.domain.auth.exception.AuthException;
import com.gaguraczi.paw.domain.auth.exception.code.AuthErrorCode;
import com.gaguraczi.paw.domain.community.dto.res.CommunityDetailRes;
import com.gaguraczi.paw.domain.community.dto.res.CommunityListItemRes;
import com.gaguraczi.paw.domain.community.dto.res.CommunityTagRes;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.CommunitySort;
import com.gaguraczi.paw.domain.community.enums.CommunityTagCode;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.community.redis.CommunityCountRedisStore;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.community.repository.CommunityTagRepository;
import com.gaguraczi.paw.domain.community.support.CommunityCursorCodec;
import com.gaguraczi.paw.domain.like.repository.CommunityLikeRepository;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CommunityService {

    private static final Set<PostType> SUPPORTED_FEED_TYPES =
            EnumSet.of(PostType.COMMUNICATION, PostType.MARKET);
    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final CommunityRepository communityRepository;
    private final CommunityTagRepository communityTagRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityCountRedisStore communityCountRedisStore;
    private final SecurityUtils securityUtils;

    @Transactional(readOnly = true)
    public List<CommunityTagRes> listTags(PostType postType) {
        validateFeedPostType(postType);
        return communityTagRepository.findByPostTypeAndIsActiveTrueOrderBySortOrderAsc(postType)
                .stream()
                .map(CommunityTagRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CursorPageRes<CommunityListItemRes> list(
            PostType postType,
            CommunityTagCode tagCode,
            MarketStatus marketStatus,
            MarketTradeType tradeType,
            CommunitySort sort,
            String cursor,
            Integer size
    ) {
        validateFeedPostType(postType);
        if (tagCode != null && tagCode.getPostType() != postType) {
            throw GeneralException.of(CommunityErrorCode.TAG_TYPE_MISMATCH_400);
        }
        if (postType == PostType.COMMUNICATION) {
            marketStatus = null;
            tradeType = null;
        }
        CommunitySort resolvedSort = sort == null ? CommunitySort.LATEST : sort;
        int pageSize = normalizeSize(size);
        CommunityCursorCodec.Cursor decoded = CommunityCursorCodec.decode(cursor, resolvedSort);
        String tagCodeValue = tagCode == null ? null : tagCode.name();

        List<Community> rows = findFeedRows(
                postType,
                tagCodeValue,
                marketStatus,
                tradeType,
                resolvedSort,
                decoded,
                pageSize + 1
        );

        boolean hasNext = rows.size() > pageSize;
        List<Community> page = hasNext ? rows.subList(0, pageSize) : rows;
        Map<Long, Long> viewCounts = communityCountRedisStore.getViewCounts(page);
        Map<Long, Long> likeCounts = communityCountRedisStore.getLikeCounts(page);

        List<CommunityListItemRes> content = new ArrayList<>(page.size());
        for (Community community : page) {
            content.add(CommunityListItemRes.from(
                    community,
                    viewCounts.getOrDefault(community.getPostId(), community.getViewCount()),
                    likeCounts.getOrDefault(community.getPostId(), community.getLikeCount())
            ));
        }

        String nextCursor = null;
        if (hasNext && !page.isEmpty()) {
            Community last = page.getLast();
            nextCursor = encodeNextCursor(
                    resolvedSort,
                    last,
                    viewCounts.getOrDefault(last.getPostId(), last.getViewCount()),
                    likeCounts.getOrDefault(last.getPostId(), last.getLikeCount())
            );
        }
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    @Transactional(readOnly = true)
    public CommunityDetailRes getDetail(Long postId) {
        Community community = communityRepository.findDetailById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        if (!SUPPORTED_FEED_TYPES.contains(community.getPostType())) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }

        UUID viewerUid = null;
        try {
            viewerUid = securityUtils.currentUid();
        } catch (AuthException e) {
            if (e.getCode() != AuthErrorCode.LOGIN_LINK_400) {
                throw e;
            }
        }

        long viewCount = communityCountRedisStore.increaseView(community, viewerUid);
        long likeCount = communityCountRedisStore.getLikeCount(community);
        Boolean likedByMe = viewerUid == null
                ? null
                : communityLikeRepository.findByCommunity_PostIdAndUser_Uid(postId, viewerUid).isPresent();
        Boolean isMine = viewerUid == null ? null : community.getUser().getUid().equals(viewerUid);
        return CommunityDetailRes.from(community, viewCount, likeCount, likedByMe, isMine);
    }

    private List<Community> findFeedRows(
            PostType postType,
            String tagCode,
            MarketStatus marketStatus,
            MarketTradeType tradeType,
            CommunitySort sort,
            CommunityCursorCodec.Cursor decoded,
            int limit
    ) {
        var pageable = PageRequest.of(0, limit);
        return switch (sort) {
            case LATEST -> communityRepository.findFeedByLatest(
                    postType,
                    tagCode,
                    marketStatus,
                    tradeType,
                    decoded == null ? null : decoded.createdAt(),
                    decoded == null ? null : decoded.postId(),
                    pageable
            );
            case LIKE -> communityRepository.findFeedByLike(
                    postType,
                    tagCode,
                    marketStatus,
                    tradeType,
                    decoded == null ? null : decoded.sortValue(),
                    decoded == null ? null : decoded.postId(),
                    pageable
            );
            case VIEW -> communityRepository.findFeedByView(
                    postType,
                    tagCode,
                    marketStatus,
                    tradeType,
                    decoded == null ? null : decoded.sortValue(),
                    decoded == null ? null : decoded.postId(),
                    pageable
            );
            case COMMENT -> communityRepository.findFeedByComment(
                    postType,
                    tagCode,
                    marketStatus,
                    tradeType,
                    decoded == null ? null : decoded.sortValue(),
                    decoded == null ? null : decoded.postId(),
                    pageable
            );
        };
    }

    private String encodeNextCursor(CommunitySort sort, Community last, long effectiveViewCount, long effectiveLikeCount) {
        return switch (sort) {
            case LATEST -> CommunityCursorCodec.encodeLatest(last.getCreatedAt(), last.getPostId());
            case LIKE -> CommunityCursorCodec.encodeByCount(sort, effectiveLikeCount, last.getPostId());
            case VIEW -> CommunityCursorCodec.encodeByCount(sort, effectiveViewCount, last.getPostId());
            case COMMENT -> CommunityCursorCodec.encodeByCount(sort, last.getCommentCount(), last.getPostId());
        };
    }

    private void validateFeedPostType(PostType postType) {
        if (postType == null || !SUPPORTED_FEED_TYPES.contains(postType)) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
