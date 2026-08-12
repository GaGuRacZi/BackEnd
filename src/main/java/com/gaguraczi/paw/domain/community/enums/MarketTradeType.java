package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장터 거래 유형")
public enum MarketTradeType {
    @Schema(description = "나눔")
    SHARE,
    @Schema(description = "판매")
    SELL,
    @Schema(description = "교환")
    EXCHANGE,
    @Schema(description = "구해요")
    WANT
}
