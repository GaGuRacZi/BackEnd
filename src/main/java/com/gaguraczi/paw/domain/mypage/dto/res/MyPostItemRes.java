package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.community.entity.Community;
import com.gaguraczi.paw.domain.community.enums.PostType;

import java.time.LocalDateTime;

public record MyPostItemRes(
        Long postId,
        PostType postType,
        String tagName,
        Long commentCount,
        Long likeCount,
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
