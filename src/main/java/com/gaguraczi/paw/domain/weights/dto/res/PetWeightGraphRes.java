package com.gaguraczi.paw.domain.weights.dto.res;

import com.gaguraczi.paw.domain.weights.enums.WeightGraphPeriodEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Schema(name = "PetWeightGraphRes", description = "날짜별 체중 그래프 응답")
public record PetWeightGraphRes(

        @Schema(description = "조회 기간", example = "ONE_MONTH")
        WeightGraphPeriodEnum period,

        @Schema(description = "조회 시작일", example = "2026-06-06")
        LocalDate startDate,

        @Schema(description = "조회 종료일", example = "2026-07-06")
        LocalDate endDate,

        @Schema(description = "구간 내 최저 몸무게(kg)", example = "3.90")
        BigDecimal minWeight,

        @Schema(description = "구간 내 최고 몸무게(kg)", example = "4.30")
        BigDecimal maxWeight,

        @Schema(description = "날짜순(오름차순) 그래프 포인트")
        List<PetWeightPointRes> points
) {

    public static PetWeightGraphRes of(
            WeightGraphPeriodEnum period,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal minWeight,
            BigDecimal maxWeight,
            List<PetWeightPointRes> points
    ) {
        return new PetWeightGraphRes(period, startDate, endDate, minWeight, maxWeight, points);
    }
}
