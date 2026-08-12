package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "커뮤니티 목록 정렬. 기본 LATEST. sort 변경 시 기존 cursor는 무효.")
public enum CommunitySort {
    @Schema(description = "최신순 (createdAt DESC)")
    LATEST,
    @Schema(description = "좋아요 많은 순")
    LIKE,
    @Schema(description = "조회수 많은 순")
    VIEW,
    @Schema(description = "댓글 많은 순")
    COMMENT
}
