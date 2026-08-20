package com.gaguraczi.paw.domain.weights.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(name = "PetWeightPointRes", description = "체중 그래프의 점 하나 (날짜별)")
public record PetWeightPointRes(

        @Schema(description = "기준 날짜", example = "2026-07-06")
        LocalDate date,

        @Schema(description = "해당 날짜(또는 월)의 대표 몸무게(kg)", example = "4.20")
        BigDecimal weight
) {

    public static PetWeightPointRes of(LocalDate date, BigDecimal weight) {
        return new PetWeightPointRes(date, weight);
    }
}
