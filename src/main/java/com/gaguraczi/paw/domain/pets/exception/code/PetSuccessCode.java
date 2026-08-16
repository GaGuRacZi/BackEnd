package com.gaguraczi.paw.domain.pets.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PetSuccessCode implements BaseSuccessCode {

    PET_CREATE_200(HttpStatus.OK, "PET_CREATE_200", "펫이 등록되었습니다."),
    PET_UPDATE_200(HttpStatus.OK, "PET_UPDATE_200", "펫 정보가 수정되었습니다."),
    PET_LIST_200(HttpStatus.OK, "PET_LIST_200", "반려동물 목록 조회에 성공했습니다."),
    PET_GET_200(HttpStatus.OK, "PET_GET_200", "반려동물 조회에 성공했습니다."),
    PET_MAIN_UPDATE_200(HttpStatus.OK, "PET_MAIN_UPDATE_200", "대표 반려동물이 변경되었습니다."),
    PET_DELETE_200(HttpStatus.OK, "PET_DELETE_200", "반려동물이 삭제되었습니다."),
    PET_REGISTRATION_GET_200(HttpStatus.OK, "PET_REGISTRATION_GET_200", "동물등록증 조회에 성공했습니다."),
    PET_REGISTRATION_UPDATE_200(HttpStatus.OK, "PET_REGISTRATION_UPDATE_200", "동물등록증 정보가 저장되었습니다."),
    PET_CODE_LIST_200(HttpStatus.OK, "PET_CODE_LIST_200", "코드 목록 조회에 성공했습니다."),
    PET_CODE_GET_200(HttpStatus.OK, "PET_CODE_GET_200", "선택 목록 조회에 성공했습니다."),
    PET_CODE_UPDATE_200(HttpStatus.OK, "PET_CODE_UPDATE_200", "선택 목록이 저장되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
