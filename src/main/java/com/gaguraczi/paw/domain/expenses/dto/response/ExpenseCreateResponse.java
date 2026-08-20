package com.gaguraczi.paw.domain.expenses.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gaguraczi.paw.domain.expenses.entity.ExpenseEntity;
import com.gaguraczi.paw.domain.expenses.enums.PaymentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;

@Schema(description = "의료비 기록 등록 응답")
public record ExpenseCreateResponse(

        @Schema(description = "의료비 id", example = "1")
        Long expenseId,

        @Schema(description = "반려동물 id", example = "1")
        Long petId,

        @Schema(description = "방문/구매처", example = "행복동물병원")
        String expenseName,

        @Schema(description = "이용 날짜", example = "2026-07-06")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate expenseDate,

        @Schema(description = "결제수단", example = "EASY_PAY")
        PaymentTypeEnum paymentType,

        @Schema(description = "결제수단 표시명", example = "간편결제")
        String paymentTypeLabel,

        @Schema(description = "총 결제금액(세부항목 합계)", example = "77000")
        Long totalAmount
) {

    public static ExpenseCreateResponse from(ExpenseEntity expense,Long totalAmount) {
        return new ExpenseCreateResponse(
                expense.getExpenseId(),
                expense.getPet().getPetId(),
                expense.getExpenseName(),
                expense.getExpenseDate().toLocalDate(),
                expense.getPaymentType(),
                expense.getPaymentType().getLabel(),
                totalAmount
        );
    }
}
