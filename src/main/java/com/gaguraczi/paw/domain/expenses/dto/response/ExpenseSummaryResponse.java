package com.gaguraczi.paw.domain.expenses.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의료비 요약 응답 (이번 달 병원비 + 누적 총 병원비)")
public record ExpenseSummaryResponse(

        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "7")
        int month,

        @Schema(description = "지정 연월의 병원비 합계(원). 기록 없으면 0", example = "124000")
        Long monthlyTotalAmount,

        @Schema(description = "해당 펫 누적 총 병원비(원). 연월과 무관. 기록 없으면 0", example = "980000")
        Long totalAmount
) {
}
