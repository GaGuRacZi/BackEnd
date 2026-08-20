package com.gaguraczi.paw.domain.mypage.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "문의 유형")
public enum InquiryType {
    @Schema(description = "계정")
    ACCOUNT,
    @Schema(description = "결제/구독")
    PAYMENT,
    @Schema(description = "반려동물")
    PET,
    @Schema(description = "커뮤니티")
    COMMUNITY,
    @Schema(description = "기타")
    ETC
}
