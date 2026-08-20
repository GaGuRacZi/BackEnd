package com.gaguraczi.paw.domain.expenses.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ExpenseSuccessCode implements BaseSuccessCode {

    EXPENSE_CREATE_200(HttpStatus.OK, "EXPENSE_CREATE_200", "의료비 기록이 저장되었습니다."),
    EXPENSE_UPDATE_200(HttpStatus.OK, "EXPENSE_UPDATE_200", "의료비 기록이 수정되었습니다."),
    EXPENSE_DELETE_200(HttpStatus.OK, "EXPENSE_DELETE_200", "의료비 기록이 삭제되었습니다."),
    EXPENSE_LIST_200(HttpStatus.OK, "EXPENSE_LIST_200", "월별 의료비 내역을 조회했습니다."),
    EXPENSE_SUMMARY_200(HttpStatus.OK, "EXPENSE_SUMMARY_200", "의료비 요약을 조회했습니다."),
    EXPENSE_DETAIL_200(HttpStatus.OK, "EXPENSE_DETAIL_200", "의료비 상세 내역을 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
