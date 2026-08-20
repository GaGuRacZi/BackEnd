package com.gaguraczi.paw.domain.expenses.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "월별 의료비 내역 응답")
public record ExpenseMonthlyResponse(

        @Schema(description = "조회 연도", example = "2026")
        int year,

        @Schema(description = "조회 월", example = "7")
        int month,

        @Schema(description = "해당 월 총 병원비", example = "124000")
        Long monthlyTotalAmount,

        @Schema(description = "의료비 내역 목록 (최신순)")
        List<ExpenseItemResponse> expenses
) {

    public static ExpenseMonthlyResponse of(int year, int month, List<ExpenseItemResponse> expenses) {
        long monthlyTotalAmount = expenses.stream()
                .mapToLong(ExpenseItemResponse::expenseAmount)
                .sum();
        return new ExpenseMonthlyResponse(year, month, monthlyTotalAmount, expenses);
    }
}
