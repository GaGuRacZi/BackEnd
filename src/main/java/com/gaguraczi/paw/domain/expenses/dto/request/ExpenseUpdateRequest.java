package com.gaguraczi.paw.domain.expenses.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gaguraczi.paw.domain.expenses.enums.PaymentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "의료비 기록 수정 요청 (보낸 값만 반영)")
public record ExpenseUpdateRequest(

        @Schema(description = "결제 금액", example = "77000")
        @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
        Long expenseAmount,

        @Schema(description = "이용 날짜", example = "2026-07-06")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate expenseDate,

        @Schema(description = "결제수단", example = "EASY_PAY",
                allowableValues = {"CARD", "TRANSFER", "VIRTUAL_ACCOUNT", "MOBILE", "EASY_PAY"})
        PaymentTypeEnum paymentType,

        @Schema(description = "방문 병원/구매처", example = "행복동물병원")
        @Size(max = 255, message = "병원명은 255자 이하여야 합니다.")
        @Pattern(regexp = "\\S(.*\\S)?", message = "병원명은 공백일 수 없습니다.")
        String expenseName,

        @Schema(description = "세부 항목 목록. 보내면 기존 항목 전체를 이 목록으로 교체합니다 (생략 시 기존 항목 유지).")
        @Valid
        List<ExpenseDetailCreateRequest> expenseDetails
) {
}
