package com.gaguraczi.paw.domain.billing.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "결제 상태. 모의 결제는 항상 SUCCESS")
public enum PaymentStatus {
    @Schema(description = "결제 성공")
    SUCCESS
}
