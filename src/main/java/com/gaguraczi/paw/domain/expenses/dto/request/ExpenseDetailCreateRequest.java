package com.gaguraczi.paw.domain.expenses.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Schema(description = "의료비 세부항목 등록 요청")
public record ExpenseDetailCreateRequest(

        @Schema(description = "내역명", example = "유선종양 수술")
        @NotBlank(message = "내역명은 필수입니다.")
        @Size(max = 255, message = "내역명은 255자 이하여야 합니다.")
        String expenseDetailName,

        @Schema(description = "비용(원)", example = "42000")
        @NotNull(message = "비용은 필수입니다.")
        @Min(value = 0, message = "비용은 0원 이상이어야 합니다.")
        Integer expenseAmount
) {
}
