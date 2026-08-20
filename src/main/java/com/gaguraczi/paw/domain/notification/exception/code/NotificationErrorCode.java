package com.gaguraczi.paw.domain.notification.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationErrorCode implements BaseErrorCode {

    NOTIFICATION_INVALID_CURSOR(HttpStatus.BAD_REQUEST, "NOTI_400", "알림 커서가 올바르지 않습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_404", "알림을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
