package com.gaguraczi.paw.domain.medication.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum MedicationErrorCode implements BaseErrorCode {

    MEDICATION_QUERY_REQUIRED(HttpStatus.BAD_REQUEST, "MEDICATION_400", "검색어를 입력해 주세요."),
    MEDICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MEDICATION_404", "약물을 찾을 수 없습니다."),
    MEDICATION_TABLE_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "MEDICATION_503",
            "medication 테이블이 없습니다. rag/sql/medication.sql을 적용하거나 dump를 restore 하세요."),
    MEDICATION_STAGING_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "MEDICATION_503_1",
            "medicine_dogcat 테이블이 없습니다. src/main/resources/data/medicine.sql을 적용하세요."),
    MEDICATION_REWRITE_FAILED(HttpStatus.BAD_GATEWAY, "MEDICATION_502", "약물 설명 생성에 실패했습니다."),
    MEDICATION_EMBEDDING_FAILED(HttpStatus.BAD_GATEWAY, "MEDICATION_502_1", "약물 임베딩 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
