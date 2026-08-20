package com.gaguraczi.paw.domain.billing.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 상태")
public enum SubscriptionStatus {
    @Schema(description = "현재 플랜 이용 중")
    ACTIVE,
    @Schema(description = "다음 결제일에 다운그레이드 예약됨")
    PENDING_CHANGE
}
