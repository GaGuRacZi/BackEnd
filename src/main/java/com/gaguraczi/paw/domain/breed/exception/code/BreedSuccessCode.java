package com.gaguraczi.paw.domain.breed.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum BreedSuccessCode implements BaseSuccessCode {

    BREED_SEARCH_200(HttpStatus.OK, "BREED_SEARCH_200", "품종 조회에 성공했습니다."),
    BREED_SYNC_200(HttpStatus.OK, "BREED_SYNC_200", "품종 동기화에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
