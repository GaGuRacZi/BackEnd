package com.gaguraczi.paw.domain.like.service;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.repository.CommunityRepository;
import com.gaguraczi.paw.domain.like.dto.response.LikeToggleResponse;
import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import com.gaguraczi.paw.domain.like.exception.LikeException;
import com.gaguraczi.paw.domain.like.exception.code.LikeErrorCode;
import com.gaguraczi.paw.domain.like.repository.CommunityLikeRepository;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityLikeService {

    private final CommunityRepository communityRepository;
    private final CommunityLikeRepository communityLikeRepository;
    private final UserRepository userRepository;

    @Transactional
    public LikeToggleResponse toggleLike(Long postId, UUID uid) {
        Community community = communityRepository.findById(postId)
                .orElseThrow(() -> LikeException.of(LikeErrorCode.COMMUNITY_NOT_FOUND));

        Optional<CommunityLike> existingLike =
                communityLikeRepository.findByCommunity_PostIdAndUser_Uid(postId, uid);

        if (existingLike.isPresent()) {
            communityLikeRepository.delete(existingLike.get());
            community.decreaseLikeCount();
            return LikeToggleResponse.builder()
                    .liked(false)
                    .likeCount(community.getLikeCount())
                    .build();
        }

        CommunityLike like = CommunityLike.builder()
                .community(community)
                .user(userRepository.getReferenceById(uid))
                .build();
        communityLikeRepository.save(like);
        community.increaseLikeCount();

        return LikeToggleResponse.builder()
                .liked(true)
                .likeCount(community.getLikeCount())
                .build();
    }
}
