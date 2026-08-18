package com.gaguraczi.paw.domain.medication.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MedicationSuccessCode implements BaseSuccessCode {

    MEDICATION_SEARCH_200(HttpStatus.OK, "MEDICATION_SEARCH_200", "약물 검색에 성공했습니다."),
    MEDICATION_GET_200(HttpStatus.OK, "MEDICATION_GET_200", "약물 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
