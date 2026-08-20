package com.gaguraczi.paw.domain.expenses.dto.response;

import com.gaguraczi.paw.domain.expenses.enums.ExpenseTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "항목별 지출 (원형 차트 한 조각)")
public record ExpenseCategoryResponse(

        @Schema(description = "비용 타입", example = "SURGERY")
        ExpenseTypeEnum expenseType,

        @Schema(description = "비용 타입 표시명", example = "수술")
        String expenseTypeLabel,

        @Schema(description = "합계 금액", example = "42000")
        Long amount,

        @Schema(description = "비율(%) - 소수점 첫째 자리까지", example = "54.5")
        Double ratio
) {

    public static ExpenseCategoryResponse of(ExpenseTypeEnum expenseType, long amount, long totalAmount) {
        double ratio = (totalAmount <= 0)
                ? 0.0
                : Math.round((amount * 1000.0) / totalAmount) / 10.0;
        return new ExpenseCategoryResponse(expenseType, expenseType.getLabel(), amount, ratio);
    }
}
