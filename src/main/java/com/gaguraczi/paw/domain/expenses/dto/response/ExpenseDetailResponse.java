package com.gaguraczi.paw.domain.expenses.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gaguraczi.paw.domain.expenses.entity.ExpenseEntity;
import com.gaguraczi.paw.domain.expenses.enums.PaymentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "의료비 등록/수정/상세 응답")
public record ExpenseDetailResponse(

        @Schema(description = "의료비 id", example = "1")
        Long expenseId,

        @Schema(description = "반려동물 id", example = "1")
        Long petId,

        @Schema(description = "결제 금액", example = "77000")
        Long expenseAmount,

        @Schema(description = "이용 날짜", example = "2026-07-06")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate expenseDate,

        @Schema(description = "결제수단 enum. 수정 폼에 이 값을 넣으세요", example = "EASY_PAY")
        PaymentTypeEnum paymentType,

        @Schema(description = "결제수단 한글 표시명. UI 라벨용", example = "간편결제")
        String paymentTypeLabel,

        @Schema(description = "방문/구매처", example = "행복동물병원")
        String expenseName,

        @Schema(description = "세부 항목 목록")
        List<ExpenseDetailItemResponse> expenseDetails
) {

    public static ExpenseDetailResponse from(ExpenseEntity expense) {
        List<ExpenseDetailItemResponse> details = expense.getExpenseDetails().stream()
                .map(ExpenseDetailItemResponse::from)
                .toList();

        return new ExpenseDetailResponse(
                expense.getExpenseId(),
                expense.getPet().getPetId(),
                expense.getExpenseAmount(),
                expense.getExpenseDate().toLocalDate(),
                expense.getPaymentType(),
                expense.getPaymentType().getLabel(),
                expense.getExpenseName(),
                details
        );
    }
}
