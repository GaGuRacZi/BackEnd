package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장터 게시글 상태")
public enum MarketStatus {
    @Schema(description = "거래중")
    IN_PROGRESS,
    @Schema(description = "예약중")
    RESERVED,
    @Schema(description = "거래완료")
    COMPLETED
}
