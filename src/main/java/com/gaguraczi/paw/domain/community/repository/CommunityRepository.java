package com.gaguraczi.paw.domain.community.repository;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.MarketStatus;
import com.gaguraczi.paw.domain.community.enums.MarketTradeType;
import com.gaguraczi.paw.domain.community.enums.PostType;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityRepository extends JpaRepository<Community, Long> {

    @EntityGraph(attributePaths = {"communityTag", "user", "region"})
    @Query("""
            SELECT c FROM Community c
            WHERE c.postType = :postType
              AND (:tagCode IS NULL OR c.communityTag.tagCode = :tagCode)
              AND (:marketStatus IS NULL OR c.marketStatus = :marketStatus)
              AND (:tradeType IS NULL OR c.tradeType = :tradeType)
              AND (
                    :cursorCreatedAt IS NULL
                    OR c.createdAt < :cursorCreatedAt
                    OR (c.createdAt = :cursorCreatedAt AND c.postId < :cursorPostId)
                  )
            ORDER BY c.createdAt DESC, c.postId DESC
            """)
    List<Community> findFeedByLatest(
            @Param("postType") PostType postType,
            @Param("tagCode") String tagCode,
            @Param("marketStatus") MarketStatus marketStatus,
            @Param("tradeType") MarketTradeType tradeType,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"communityTag", "user", "region"})
    @Query("""
            SELECT c FROM Community c
            WHERE c.postType = :postType
              AND (:tagCode IS NULL OR c.communityTag.tagCode = :tagCode)
              AND (:marketStatus IS NULL OR c.marketStatus = :marketStatus)
              AND (:tradeType IS NULL OR c.tradeType = :tradeType)
              AND (
                    :cursorLikeCount IS NULL
                    OR c.likeCount < :cursorLikeCount
                    OR (c.likeCount = :cursorLikeCount AND c.postId < :cursorPostId)
                  )
            ORDER BY c.likeCount DESC, c.postId DESC
            """)
    List<Community> findFeedByLike(
            @Param("postType") PostType postType,
            @Param("tagCode") String tagCode,
            @Param("marketStatus") MarketStatus marketStatus,
            @Param("tradeType") MarketTradeType tradeType,
            @Param("cursorLikeCount") Long cursorLikeCount,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"communityTag", "user", "region"})
    @Query("""
            SELECT c FROM Community c
            WHERE c.postType = :postType
              AND (:tagCode IS NULL OR c.communityTag.tagCode = :tagCode)
              AND (:marketStatus IS NULL OR c.marketStatus = :marketStatus)
              AND (:tradeType IS NULL OR c.tradeType = :tradeType)
              AND (
                    :cursorViewCount IS NULL
                    OR c.viewCount < :cursorViewCount
                    OR (c.viewCount = :cursorViewCount AND c.postId < :cursorPostId)
                  )
            ORDER BY c.viewCount DESC, c.postId DESC
            """)
    List<Community> findFeedByView(
            @Param("postType") PostType postType,
            @Param("tagCode") String tagCode,
            @Param("marketStatus") MarketStatus marketStatus,
            @Param("tradeType") MarketTradeType tradeType,
            @Param("cursorViewCount") Long cursorViewCount,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"communityTag", "user", "region"})
    @Query("""
            SELECT c FROM Community c
            WHERE c.postType = :postType
              AND (:tagCode IS NULL OR c.communityTag.tagCode = :tagCode)
              AND (:marketStatus IS NULL OR c.marketStatus = :marketStatus)
              AND (:tradeType IS NULL OR c.tradeType = :tradeType)
              AND (
                    :cursorCommentCount IS NULL
                    OR c.commentCount < :cursorCommentCount
                    OR (c.commentCount = :cursorCommentCount AND c.postId < :cursorPostId)
                  )
            ORDER BY c.commentCount DESC, c.postId DESC
            """)
    List<Community> findFeedByComment(
            @Param("postType") PostType postType,
            @Param("tagCode") String tagCode,
            @Param("marketStatus") MarketStatus marketStatus,
            @Param("tradeType") MarketTradeType tradeType,
            @Param("cursorCommentCount") Long cursorCommentCount,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"communityTag", "user", "region"})
    @Query("""
            SELECT c FROM Community c
            WHERE c.user.uid = :uid
              AND c.postType IN :postTypes
              AND (
                    :cursorCreatedAt IS NULL
                    OR c.createdAt < :cursorCreatedAt
                    OR (c.createdAt = :cursorCreatedAt AND c.postId < :cursorPostId)
                  )
            ORDER BY c.createdAt DESC, c.postId DESC
            """)
    List<Community> findMyPosts(
            @Param("uid") UUID uid,
            @Param("postTypes") Collection<PostType> postTypes,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorPostId") Long cursorPostId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"communityTag", "user", "region", "photos"})
    @Query("SELECT c FROM Community c WHERE c.postId = :postId")
    Optional<Community> findDetailById(@Param("postId") Long postId);
}
