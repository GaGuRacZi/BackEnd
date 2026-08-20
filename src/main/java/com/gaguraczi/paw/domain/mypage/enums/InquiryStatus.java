package com.gaguraczi.paw.domain.mypage.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문의 처리 상태. 등록 직후는 RECEIVED")
public enum InquiryStatus {
    @Schema(description = "접수")
    RECEIVED,
    @Schema(description = "처리중")
    IN_PROGRESS,
    @Schema(description = "답변 완료")
    ANSWERED
}
