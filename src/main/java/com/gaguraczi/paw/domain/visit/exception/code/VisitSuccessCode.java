package com.gaguraczi.paw.domain.visit.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum VisitSuccessCode implements BaseSuccessCode {

    VISIT_CREATE_200(HttpStatus.OK, "VISIT_CREATE_200", "진료 기록이 등록되었습니다."),
    VISIT_LIST_200(HttpStatus.OK, "VISIT_LIST_200", "진료 목록 조회에 성공했습니다."),
    VISIT_GET_200(HttpStatus.OK, "VISIT_GET_200", "진료 조회에 성공했습니다."),
    VISIT_TRANSCRIPT_200(HttpStatus.OK, "VISIT_TRANSCRIPT_200", "전사문 조회에 성공했습니다."),
    VISIT_PRESCRIPTION_ADD_200(HttpStatus.OK, "VISIT_PRESCRIPTION_ADD_200", "약물이 추가되었습니다."),
    VISIT_PRESCRIPTION_DELETE_200(HttpStatus.OK, "VISIT_PRESCRIPTION_DELETE_200", "약물이 삭제되었습니다."),
    VISIT_AI_SUMMARY_200(HttpStatus.OK, "VISIT_AI_SUMMARY_200", "AI 진료 요약을 생성했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
