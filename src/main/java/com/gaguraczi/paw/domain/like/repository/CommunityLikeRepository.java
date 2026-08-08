package com.gaguraczi.paw.domain.like.repository;

import com.gaguraczi.paw.domain.like.entity.CommunityLike;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommunityLikeRepository extends JpaRepository<CommunityLike, Long> {

    Optional<CommunityLike> findByCommunity_PostIdAndUser_Uid(Long postId, UUID uid);

    @EntityGraph(attributePaths = {"community", "community.communityTag", "community.user"})
    @Query("""
            SELECT l FROM CommunityLike l
            WHERE l.user.uid = :uid
              AND (
                    :cursorCreatedAt IS NULL
                    OR l.createdAt < :cursorCreatedAt
                    OR (l.createdAt = :cursorCreatedAt AND l.communityLikeId < :cursorLikeId)
                  )
            ORDER BY l.createdAt DESC, l.communityLikeId DESC
            """)
    List<CommunityLike> findMyLikes(
            @Param("uid") UUID uid,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorLikeId") Long cursorLikeId,
            Pageable pageable
    );
}
