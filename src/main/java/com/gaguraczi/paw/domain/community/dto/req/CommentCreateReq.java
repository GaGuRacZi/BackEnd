package com.gaguraczi.paw.domain.community.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 작성 요청")
public record CommentCreateReq(
        @NotBlank
        @Schema(description = "댓글 내용", example = "좋은 정보 감사합니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,

        @Schema(description = "부모 댓글 ID (없으면 루트 댓글, 있으면 대댓글)", example = "1", nullable = true)
        Long parentId
) {
}
