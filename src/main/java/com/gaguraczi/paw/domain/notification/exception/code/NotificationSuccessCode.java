package com.gaguraczi.paw.domain.notification.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationSuccessCode implements BaseSuccessCode {

    DEVICE_TOKEN_REGISTER_200(HttpStatus.OK, "DEVICE_TOKEN_REGISTER_200", "디바이스 토큰이 등록되었습니다."),
    DEVICE_TOKEN_DELETE_200(HttpStatus.OK, "DEVICE_TOKEN_DELETE_200", "디바이스 토큰이 삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
