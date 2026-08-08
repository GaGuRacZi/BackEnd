package com.gaguraczi.paw.domain.community.dto.res;

import com.gaguraczi.paw.domain.community.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "댓글 응답 (flat + parentId)")
public class CommentRes {

    @Schema(example = "1")
    private final Long commentId;
    @Schema(example = "10")
    private final Long postId;
    @Schema(description = "부모 댓글 ID (루트면 null)", nullable = true)
    private final Long parentId;
    @Schema(description = "삭제된 댓글은 null", example = "좋은 정보 감사합니다")
    private final String content;
    @Schema(description = "soft delete 여부", example = "false")
    private final boolean deleted;
    @Schema(example = "길동이")
    private final String authorNickname;
    @Schema(example = "2026-08-08T11:00:00")
    private final LocalDateTime createdAt;

    public static CommentRes from(Comment comment) {
        boolean deleted = Boolean.TRUE.equals(comment.getIsDel());
        return CommentRes.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getCommunity().getPostId())
                .parentId(comment.getParent() == null ? null : comment.getParent().getCommentId())
                .content(deleted ? null : comment.getContent())
                .deleted(deleted)
                .authorNickname(comment.getUser().getNickname())
                .createdAt(comment.getCreatedAt())
                .build();
    }
}
