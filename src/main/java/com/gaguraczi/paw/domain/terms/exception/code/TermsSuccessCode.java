package com.gaguraczi.paw.domain.terms.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TermsSuccessCode implements BaseSuccessCode {

    TERMS_LIST_200(HttpStatus.OK, "TERMS_LIST_200", "약관 목록 조회에 성공했습니다."),
    TERMS_DETAIL_200(HttpStatus.OK, "TERMS_DETAIL_200", "약관 상세 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
