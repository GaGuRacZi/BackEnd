package com.gaguraczi.paw.domain.community.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "장터 거래 방법")
public enum MarketTradeMethod {
    @Schema(description = "직거래")
    DIRECT,
    @Schema(description = "택배")
    DELIVERY,
    @Schema(description = "비대면 나눔")
    CONTACTLESS_SHARE
}
