package com.gaguraczi.paw.domain.expenses.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExpenseErrorCode implements BaseErrorCode {

    EXPENSE_NOT_FOUND(HttpStatus.NOT_FOUND, "EXPENSE_404", "의료비 기록을 찾을 수 없습니다."),
    EXPENSE_DETAIL_REQUIRED(HttpStatus.BAD_REQUEST, "EXPENSE_400_1", "세부 항목은 최소 1개 이상이어야 합니다."),
    EXPENSE_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "EXPENSE_400_2", "조회 연월이 올바르지 않습니다."),
    EXPENSE_FUTURE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "EXPENSE_400_3", "미래 날짜로는 의료비를 기록할 수 없습니다."),
    EXPENSE_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "EXPENSE_400_4", "결제 금액과 세부 항목 금액 합계가 일치하지 않습니다."),
    EXPENSE_FORBIDDEN(HttpStatus.FORBIDDEN, "EXPENSE_403", "권한이 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
