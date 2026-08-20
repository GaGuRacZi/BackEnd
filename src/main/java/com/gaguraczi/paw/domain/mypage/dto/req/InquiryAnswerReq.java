package com.gaguraczi.paw.domain.mypage.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "관리자 문의 답변")
public record InquiryAnswerReq(
        @NotBlank(message = "답변 내용은 필수입니다.")
        @Schema(description = "답변 내용", example = "결제 내역을 확인했습니다. 재시도해 주세요.",
                requiredMode = Schema.RequiredMode.REQUIRED)
        String answer
) {
}
