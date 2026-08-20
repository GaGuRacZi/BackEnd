package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "마이페이지 커뮤니티 글 요약 (작성글/찜)")
public record MyPostItemRes(
        @Schema(description = "게시글 ID", example = "10")
        Long postId,
        @Schema(description = "게시글 유형", example = "COMMUNICATION")
        PostType postType,
        @Schema(description = "태그 표시명", example = "건강상담")
        String tagName,
        @Schema(description = "댓글 수", example = "4")
        Long commentCount,
        @Schema(description = "좋아요(찜) 수", example = "12")
        Long likeCount,
        @Schema(description = "작성 시각", example = "2026-08-20T15:00:00")
        LocalDateTime createdAt
) {
    public static MyPostItemRes from(Community community) {
        return new MyPostItemRes(
                community.getPostId(),
                community.getPostType(),
                community.getCommunityTag().getTagName(),
                community.getCommentCount(),
                community.getLikeCount(),
                community.getCreatedAt()
        );
    }
}
