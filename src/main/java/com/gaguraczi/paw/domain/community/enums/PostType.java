package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시글 유형. 피드/작성 API는 COMMUNICATION, MARKET만 지원. REVIEW는 태그 시드만 존재.")
public enum PostType {
    @Schema(description = "소통")
    COMMUNICATION,
    @Schema(description = "장터")
    MARKET,
    @Schema(description = "후기 (피드 API 미지원)")
    REVIEW
}
