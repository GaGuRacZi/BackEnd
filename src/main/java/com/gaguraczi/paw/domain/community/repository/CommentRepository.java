package com.gaguraczi.paw.domain.community.repository;

import com.gaguraczi.paw.domain.community.entity.Comment;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @EntityGraph(attributePaths = {"user", "parent", "community"})
    @Query("""
            SELECT c FROM Comment c
            WHERE c.community.postId = :postId
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR c.createdAt < :cursorCreatedAt
                    OR (c.createdAt = :cursorCreatedAt AND c.commentId < :cursorCommentId)
                  )
            ORDER BY c.createdAt DESC, c.commentId DESC
            """)
    List<Comment> findByPost(
            @Param("postId") Long postId,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorCommentId") Long cursorCommentId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = {"user", "parent", "parent.user", "community", "community.user"})
    @Query("SELECT c FROM Comment c WHERE c.commentId = :commentId")
    Optional<Comment> findDetailById(@Param("commentId") Long commentId);

    @Query("""
            SELECT c FROM Comment c
            WHERE c.user.uid = :uid
              AND c.isDel = false
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR c.createdAt < :cursorCreatedAt
                    OR (c.createdAt = :cursorCreatedAt AND c.commentId < :cursorCommentId)
                  )
            ORDER BY c.createdAt DESC, c.commentId DESC
            """)
    List<Comment> findMyComments(
            @Param("uid") UUID uid,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorCommentId") Long cursorCommentId,
            Pageable pageable
    );
}
