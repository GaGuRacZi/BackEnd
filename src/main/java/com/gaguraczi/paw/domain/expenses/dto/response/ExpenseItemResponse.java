package com.gaguraczi.paw.domain.expenses.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gaguraczi.paw.domain.expenses.entity.ExpenseEntity;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "의료비 내역 목록 항목")
public record ExpenseItemResponse(

        @Schema(description = "의료비 id", example = "1")
        Long expenseId,

        @Schema(description = "방문/구매처", example = "행복동물병원")
        String expenseName,

        @Schema(description = "이용 날짜", example = "2026-07-04")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate expenseDate,

        @Schema(description = "결제 금액", example = "72000")
        Long expenseAmount
) {

    public static ExpenseItemResponse from(ExpenseEntity expense) {
        return new ExpenseItemResponse(
                expense.getExpenseId(),
                expense.getExpenseName(),
                expense.getExpenseDate().toLocalDate(),
                expense.getExpenseAmount()
        );
    }
}
