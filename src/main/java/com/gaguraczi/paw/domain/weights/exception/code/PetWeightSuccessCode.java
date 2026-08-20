package com.gaguraczi.paw.domain.weights.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PetWeightSuccessCode implements BaseSuccessCode {

    PET_WEIGHT_CREATE_200(HttpStatus.OK, "PET_WEIGHT_CREATE_200", "체중 기록이 저장되었습니다."),
    PET_WEIGHT_UPDATE_200(HttpStatus.OK, "PET_WEIGHT_UPDATE_200", "체중 기록이 수정되었습니다."),
    PET_WEIGHT_DELETE_200(HttpStatus.OK, "PET_WEIGHT_DELETE_200", "체중 기록이 삭제되었습니다."),
    PET_WEIGHT_GET_200(HttpStatus.OK, "PET_WEIGHT_GET_200", "체중 기록 조회에 성공했습니다."),
    PET_WEIGHT_LIST_200(HttpStatus.OK, "PET_WEIGHT_LIST_200", "체중 기록 목록 조회에 성공했습니다."),
    PET_WEIGHT_GRAPH_200(HttpStatus.OK, "PET_WEIGHT_GRAPH_200", "체중 그래프 조회에 성공했습니다."),
    PET_WEIGHT_SUMMARY_200(HttpStatus.OK, "PET_WEIGHT_SUMMARY_200", "체중 요약 조회에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
