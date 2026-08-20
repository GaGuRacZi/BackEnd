package com.gaguraczi.paw.domain.billing.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 유형")
public enum PaymentType {
    @Schema(description = "요금제 변경(업그레이드) 결제")
    PURCHASE,
    @Schema(description = "월 갱신 결제")
    RENEWAL
}
