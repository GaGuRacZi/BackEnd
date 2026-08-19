package com.gaguraczi.paw.domain.notification.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum NotificationErrorCode implements BaseErrorCode {

    TOKEN_REQUIRED_400(HttpStatus.BAD_REQUEST, "NOTIFICATION_400_1", "디바이스 토큰이 필요합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
