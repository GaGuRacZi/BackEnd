package com.gaguraczi.paw.domain.breed.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BreedErrorCode implements BaseErrorCode {

    BREED_TYPE_REQUIRED(HttpStatus.BAD_REQUEST, "BREED_400", "반려동물 종류(petType)는 필수입니다."),
    BREED_NOT_FOUND(HttpStatus.BAD_REQUEST, "BREED_400_1", "존재하지 않는 품종입니다."),
    BREED_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "BREED_400_2", "선택한 품종이 반려동물 종류와 일치하지 않습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
