package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record MyCommentItemRes(
        Long postId,
        String title,
        String commentPreview,
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
