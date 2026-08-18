package com.gaguraczi.paw.domain.weights.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PetWeightErrorCode implements BaseErrorCode {

    PET_WEIGHT_400(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400", "체중 기록 요청 처리에 실패했습니다."),
    PET_WEIGHT_FUTURE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_1", "미래 날짜로는 체중을 기록할 수 없습니다."),
    PET_WEIGHT_INVALID_PERIOD(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_2", "조회 기간이 올바르지 않습니다."),
    PET_WEIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_WEIGHT_404", "체중 기록을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
