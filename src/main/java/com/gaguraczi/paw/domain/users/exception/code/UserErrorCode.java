package com.gaguraczi.paw.domain.users.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserErrorCode implements BaseErrorCode {

    USER_PROFILE_400(HttpStatus.BAD_REQUEST, "USER_PROFILE_400", "프로필 수정에 실패했습니다."),
    USER_PROFILE_IMAGE_EMPTY(HttpStatus.BAD_REQUEST, "USER_PROFILE_400_1", "비어 있는 이미지 파일은 업로드할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
