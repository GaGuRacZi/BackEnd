package com.gaguraczi.paw.domain.expenses.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의료비 요약 응답 (이번 달 병원비 + 누적 총 병원비)")
public record ExpenseSummaryResponse(

        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "7")
        int month,

        @Schema(description = "이번 달 병원비", example = "124000")
        Long monthlyTotalAmount,

        @Schema(description = "총 병원비 (누적)", example = "980000")
        Long totalAmount
) {
}
