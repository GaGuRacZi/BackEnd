package com.gaguraczi.paw.domain.expenses.dto.response;

import com.gaguraczi.paw.domain.expenses.entity.ExpenseDetailEntity;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "의료비 세부항목")
public record ExpenseDetailItemResponse(

        @Schema(description = "세부항목 id", example = "1")
        Long expenseDetailId,

        @Schema(description = "내역명", example = "유선종양 수술")
        String expenseDetailName,

        @Schema(description = "비용(원)", example = "42000")
        Integer expenseAmount
) {

    public static ExpenseDetailItemResponse from(ExpenseDetailEntity expenseDetail) {
        return new ExpenseDetailItemResponse(
                expenseDetail.getExpenseDetailId(),
                expenseDetail.getExpenseDetailName(),
                expenseDetail.getExpenseAmount()
        );
    }
}
