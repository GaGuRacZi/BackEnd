package com.gaguraczi.paw.domain.users.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum UserSuccessCode implements BaseSuccessCode {

    USER_PROFILE_200(HttpStatus.OK, "USER_PROFILE_200", "프로필 조회에 성공했습니다."),
    USER_PROFILE_UPDATE_200(HttpStatus.OK, "USER_PROFILE_UPDATE_200", "프로필이 수정되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
