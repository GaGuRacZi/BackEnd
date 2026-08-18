package com.gaguraczi.paw.domain.weights.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "체중 그래프 조회 기간")
public enum WeightGraphPeriodEnum {

    @Schema(description = "최근 1개월 (일 단위 집계)")
    ONE_MONTH(1),

    @Schema(description = "최근 6개월 (월 단위 집계)")
    SIX_MONTHS(6);

    private final int months;

    public boolean isMonthlyBucket() {
        return this == SIX_MONTHS;
    }
}
