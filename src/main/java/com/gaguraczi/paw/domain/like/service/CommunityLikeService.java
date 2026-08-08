package com.gaguraczi.paw.domain.like.service;

import com.gaguraczi.paw.domain.community.dto.res.LikeToggleRes;
import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.domain.community.redis.CommunityCountRedisStore;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import com.gaguraczi.paw.domain.like.repository.CommunityLikeRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.EnumSet;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class CommunityLikeService {

    private static final Set<PostType> SUPPORTED = EnumSet.of(PostType.COMMUNICATION, PostType.MARKET);

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final CommunityCountRedisStore communityCountRedisStore;
    private final SecurityUtils securityUtils;

    @Transactional
    public LikeToggleRes toggle(Long postId) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> GeneralException.of(CommunityErrorCode.COMMUNITY_NOT_FOUND_404));
        if (!SUPPORTED.contains(community.getPostType())) {
            throw GeneralException.of(CommunityErrorCode.POST_TYPE_UNSUPPORTED_400);
        }
        User user = securityUtils.currentUser();
        long currentLikeCount = communityCountRedisStore.getLikeCount(community);

        Optional<CommunityLike> existing =
                communityLikeRepository.findByCommunity_PostIdAndUser_Uid(postId, user.getUid());
        if (existing.isPresent()) {
            communityLikeRepository.delete(existing.get());
            long expected = Math.max(0L, currentLikeCount - 1L);
            afterCommit(() -> communityCountRedisStore.decreaseLike(community));
            return LikeToggleRes.of(false, expected);
        }

        int inserted = communityLikeRepository.insertIgnore(postId, user.getUid());
        if (inserted == 0) {
            return LikeToggleRes.of(true, currentLikeCount);
        }
        long expected = currentLikeCount + 1L;
        afterCommit(() -> communityCountRedisStore.increaseLike(community));
        return LikeToggleRes.of(true, expected);
    }

    private void afterCommit(Runnable action) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    action.run();
                }
            });
            return;
        }
        action.run();
    }
}
