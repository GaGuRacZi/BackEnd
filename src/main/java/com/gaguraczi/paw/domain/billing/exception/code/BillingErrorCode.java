package com.gaguraczi.paw.domain.billing.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BillingErrorCode implements BaseErrorCode {

    SAME_PLAN(HttpStatus.BAD_REQUEST, "BILLING_400_1", "이미 이용 중인 요금제입니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "BILLING_404_1", "사용자를 찾을 수 없습니다."),
    PAYMENT_NOT_FOUND(HttpStatus.NOT_FOUND, "BILLING_404_2", "결제 내역을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
