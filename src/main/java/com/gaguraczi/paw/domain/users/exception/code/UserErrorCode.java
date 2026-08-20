package com.gaguraczi.paw.domain.users.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_PROFILE_400(HttpStatus.BAD_REQUEST, "USER_PROFILE_400", "프로필 수정에 실패했습니다."),
    USER_PROFILE_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "USER_PROFILE_400_1", "비어 있는 이미지 파일은 업로드할 수 없습니다."),
    USER_PROFILE_IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "USER_PROFILE_400_2", "프로필 이미지는 5MB 이하여야 합니다."),
    USER_PROFILE_IMAGE_INVALID(HttpStatus.BAD_REQUEST, "USER_PROFILE_400_3", "지원하지 않는 이미지 형식입니다. JPEG, PNG, GIF, WEBP, HEIC, HEIF만 업로드할 수 있습니다."),
    USER_HARD_DELETE_SELF(HttpStatus.BAD_REQUEST, "USER_400_4", "본인 계정은 하드탈퇴할 수 없습니다."),
    USER_HARD_DELETE_ADMIN(HttpStatus.BAD_REQUEST, "USER_400_5", "관리자 계정은 하드탈퇴할 수 없습니다."),
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_404", "사용자를 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
