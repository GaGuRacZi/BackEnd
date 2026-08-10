package com.gaguraczi.paw.domain.pets.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PetSuccessCode implements BaseSuccessCode {

    PET_CREATE_200(HttpStatus.OK, "PET_CREATE_200", "펫이 등록되었습니다."),
    PET_UPDATE_200(HttpStatus.OK, "PET_UPDATE_200", "펫 정보가 수정되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
