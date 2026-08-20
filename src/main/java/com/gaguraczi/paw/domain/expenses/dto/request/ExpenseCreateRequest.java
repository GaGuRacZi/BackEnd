package com.gaguraczi.paw.domain.expenses.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.gaguraczi.paw.domain.expenses.enums.PaymentTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

@Schema(description = "의료비 기록 등록 요청")
public record ExpenseCreateRequest(

        @Schema(description = "결제 금액", example = "77000")
        @NotNull(message = "결제 금액은 필수입니다.")
        @Min(value = 0, message = "결제 금액은 0원 이상이어야 합니다.")
        Long expenseAmount,

        @Schema(description = "이용 날짜", example = "2026-07-06")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        @NotNull(message = "이용 날짜는 필수입니다.")
        LocalDate expenseDate,

        @Schema(description = "결제수단", example = "EASY_PAY",
                allowableValues = {"CARD", "TRANSFER", "VIRTUAL_ACCOUNT", "MOBILE", "EASY_PAY"})
        @NotNull(message = "결제수단은 필수입니다.")
        PaymentTypeEnum paymentType,

        @Schema(description = "방문 병원/구매처", example = "행복동물병원")
        @NotBlank(message = "병원(방문처)은 필수입니다.")
        @Size(max = 255, message = "병원명은 255자 이하여야 합니다.")
        String expenseName,

        @Schema(description = "병원 주소 (수기 입력)", example = "서울특별시 종로구 세종대로 110")
        @Size(max = 255, message = "주소는 255자 이하여야 합니다.")
        String expenseAddress,

        @Schema(description = "세부 항목 목록 (어디에 얼마를 썼는지 항목별 기록)")
        @NotEmpty(message = "세부 항목은 최소 1개 이상이어야 합니다.")
        @Valid
        List<ExpenseDetailCreateRequest> expenseDetails
) {
}
