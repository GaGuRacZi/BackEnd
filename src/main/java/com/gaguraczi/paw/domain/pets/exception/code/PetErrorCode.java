package com.gaguraczi.paw.domain.pets.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum PetErrorCode implements BaseErrorCode {

    PET_400(HttpStatus.BAD_REQUEST, "PET_400", "펫 요청 처리에 실패했습니다."),
    PET_BREED_REQUIRED(HttpStatus.BAD_REQUEST, "PET_400_1", "품종 ID 또는 품종명 중 하나는 필수입니다."),
    PET_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "PET_400_2", "비어 있는 이미지 파일은 업로드할 수 없습니다."),
    PET_BLOOD_TYPE_MISMATCH(HttpStatus.BAD_REQUEST, "PET_400_3", "반려동물 종류에 맞지 않는 혈액형입니다."),
    PET_CATEGORY_INVALID(HttpStatus.BAD_REQUEST, "PET_400_4", "지원하지 않는 카테고리입니다."),
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404", "펫을 찾을 수 없습니다."),
    PET_REGISTRATION_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_1", "등록된 동물등록증 정보가 없습니다."),
    PET_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_404_2", "존재하지 않는 코드가 포함되어 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
