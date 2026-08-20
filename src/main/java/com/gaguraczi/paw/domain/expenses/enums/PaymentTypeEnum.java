package com.gaguraczi.paw.domain.expenses.enums;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@Schema(description = "결제수단. 요청/응답 필드는 영문 enum, 화면 문구는 paymentTypeLabel 사용",
        allowableValues = {"CARD", "TRANSFER", "VIRTUAL_ACCOUNT", "MOBILE", "EASY_PAY"})
public enum PaymentTypeEnum {

    @Schema(description = "카드")
    CARD("카드"),
    @Schema(description = "계좌이체")
    TRANSFER("계좌이체"),
    @Schema(description = "가상계좌")
    VIRTUAL_ACCOUNT("가상계좌"),
    @Schema(description = "휴대폰 결제")
    MOBILE("휴대폰 결제"),
    @Schema(description = "간편결제 (카카오페이 등)")
    EASY_PAY("간편결제");

    private final String label;
}
