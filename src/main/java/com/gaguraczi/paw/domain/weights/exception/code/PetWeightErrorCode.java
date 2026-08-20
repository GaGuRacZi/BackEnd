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
    PET_WEIGHT_PHOTO_LIMIT_400(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_3", "메모 사진은 최대 3장까지 첨부할 수 있습니다."),
    PET_WEIGHT_PHOTO_EMPTY_400(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_4", "이미지 파일이 비어 있습니다."),
    PET_WEIGHT_PHOTO_TOO_LARGE_400(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_5", "이미지 용량은 5MB 이하여야 합니다."),
    PET_WEIGHT_PHOTO_INVALID_400(HttpStatus.BAD_REQUEST, "PET_WEIGHT_400_6", "지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다."),
    PET_WEIGHT_NOT_FOUND(HttpStatus.NOT_FOUND, "PET_WEIGHT_404", "체중 기록을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
