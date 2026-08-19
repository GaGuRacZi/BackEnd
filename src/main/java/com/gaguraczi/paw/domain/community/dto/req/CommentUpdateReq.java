package com.gaguraczi.paw.domain.community.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "댓글 수정 요청")
public record CommentUpdateReq(
        @NotBlank
        @Schema(description = "수정할 댓글 내용", example = "수정된 댓글 내용입니다", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
