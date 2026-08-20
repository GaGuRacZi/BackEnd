package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.community.entity.Comment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "내가 댓글 단 글 목록 아이템. commentPreview는 최대 80자.")
public record MyCommentItemRes(
        @Schema(description = "게시글 ID", example = "10")
        Long postId,
        @Schema(description = "게시글 제목", example = "산책 코스 추천해주세요")
        String title,
        @Schema(description = "내 댓글 미리보기 (최대 80자)", example = "한강공원 쪽 추천해요.")
        String commentPreview,
        @Schema(description = "댓글 작성 시각", example = "2026-08-20T16:30:00")
        LocalDateTime commentedAt
) {
    private static final int PREVIEW_LENGTH = 80;

    public static MyCommentItemRes from(Comment comment) {
        String content = comment.getContent();
        String preview = content != null && content.length() > PREVIEW_LENGTH
                ? content.substring(0, PREVIEW_LENGTH)
                : content;
        return new MyCommentItemRes(
                comment.getCommunity().getPostId(),
                comment.getCommunity().getTitle(),
                preview,
                comment.getCreatedAt()
        );
    }
}
