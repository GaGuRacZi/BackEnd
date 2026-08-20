package com.gaguraczi.paw.domain.billing.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BillingSuccessCode implements BaseSuccessCode {

    PLAN_GET_200(HttpStatus.OK, "BILLING_PLAN_200", "요금제 조회에 성공했습니다."),
    PLAN_CHANGE_200(HttpStatus.OK, "BILLING_PLAN_CHANGE_200", "요금제가 변경되었습니다."),
    PAYMENT_LIST_200(HttpStatus.OK, "BILLING_PAYMENT_LIST_200", "결제 내역 조회에 성공했습니다."),
    PAYMENT_DETAIL_200(HttpStatus.OK, "BILLING_PAYMENT_DETAIL_200", "결제 상세 조회에 성공했습니다."),
    PLAN_FORCE_200(HttpStatus.OK, "BILLING_PLAN_FORCE_200", "요금제가 강제 변경되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
