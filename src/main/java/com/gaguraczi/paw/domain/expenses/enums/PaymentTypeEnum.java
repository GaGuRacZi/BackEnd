package com.gaguraczi.paw.domain.expenses.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PaymentTypeEnum { // 결제수단

    CARD("카드"),
    TRANSFER("계좌이체"),
    VIRTUAL_ACCOUNT("가상계좌"),
    MOBILE("휴대폰 결제"),
    EASY_PAY("간편결제"); // 카카오페이 등

    private final String label;
}
