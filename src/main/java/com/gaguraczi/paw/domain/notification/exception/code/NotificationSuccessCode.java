package com.gaguraczi.paw.domain.notification.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum NotificationSuccessCode implements BaseSuccessCode {

    NOTIFICATION_LIST_200(HttpStatus.OK, "NOTI_LIST_200", "알림 목록 조회에 성공했습니다."),
    NOTIFICATION_UNREAD_COUNT_200(HttpStatus.OK, "NOTI_UNREAD_200", "미읽음 알림 수 조회에 성공했습니다."),
    NOTIFICATION_READ_200(HttpStatus.OK, "NOTI_READ_200", "알림을 읽음 처리했습니다."),
    NOTIFICATION_READ_ALL_200(HttpStatus.OK, "NOTI_READ_ALL_200", "모든 알림을 읽음 처리했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
