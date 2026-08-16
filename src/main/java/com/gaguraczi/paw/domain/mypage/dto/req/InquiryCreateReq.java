package com.gaguraczi.paw.domain.mypage.dto.req;

import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "문의 등록 요청 (multipart data part JSON)")
public record InquiryCreateReq(
        @NotNull(message = "문의 유형은 필수입니다.")
        @Schema(description = "문의 유형", example = "ACCOUNT", requiredMode = Schema.RequiredMode.REQUIRED)
        InquiryType inquiryType,

        @NotBlank(message = "문의 내용은 필수입니다.")
        @Schema(description = "문의 내용", example = "구독 결제가 반복해서 실패해요.", requiredMode = Schema.RequiredMode.REQUIRED)
        String content
) {
}
