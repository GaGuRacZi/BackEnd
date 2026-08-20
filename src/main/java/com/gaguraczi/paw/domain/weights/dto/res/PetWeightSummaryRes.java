package com.gaguraczi.paw.domain.weights.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "PetWeightSummaryRes", description = "건강요약 - 체중 대시보드 상단 카드")
public record PetWeightSummaryRes(

        @Schema(description = "반려동물 ID", example = "1")
        Long petId,

        @Schema(description = "현재 체중(kg). 기록이 없으면 펫 등록 시 체중", example = "4.20")
        BigDecimal currentWeight,

        @Schema(description = "가장 최근 기록 시각. 기록이 없으면 null", example = "2026-07-06T20:30:00", nullable = true)
        LocalDateTime lastRecordedAt,

        @Schema(description = "이번 달 증감(kg). 양수=증가. 비교 대상이 없으면 null", example = "0.10", nullable = true)
        BigDecimal monthChange
) {

    public static PetWeightSummaryRes of(
            Long petId,
            BigDecimal currentWeight,
            LocalDateTime lastRecordedAt,
            BigDecimal monthChange
    ) {
        return new PetWeightSummaryRes(petId, currentWeight, lastRecordedAt, monthChange);
    }
}