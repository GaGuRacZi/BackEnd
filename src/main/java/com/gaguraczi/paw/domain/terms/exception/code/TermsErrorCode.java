package com.gaguraczi.paw.domain.terms.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TermsErrorCode implements BaseErrorCode {

    TERMS_NOT_FOUND(HttpStatus.NOT_FOUND, "TERMS_404", "약관을 찾을 수 없습니다."),
    TERMS_REQUIRED(HttpStatus.BAD_REQUEST, "TERM_400", "필수 약관에 모두 동의해야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
