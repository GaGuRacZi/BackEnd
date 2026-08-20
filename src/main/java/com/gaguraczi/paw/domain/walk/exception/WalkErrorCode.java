package com.gaguraczi.paw.domain.walk.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum WalkErrorCode implements BaseErrorCode {


    WALK_NOT_FOUND(HttpStatus.NOT_FOUND, "WALK_404_1", "존재하지 않는 산책 기록입니다."),
    PET_NOT_FOUND(HttpStatus.NOT_FOUND, "WALK_404_2", "존재하지 않는 반려동물입니다."),
    WALK_IN_PROGRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "WALK_404_3", "진행 중인 산책이 없습니다."),


    WALK_TIME_INVALID(HttpStatus.BAD_REQUEST, "WALK_400_1", "종료 시간은 시작 시간보다 빠를 수 없습니다."),
    WALK_DATE_RANGE_INVALID(HttpStatus.BAD_REQUEST, "WALK_400_2", "조회 시작일이 종료일보다 늦을 수 없습니다."),
    WALK_ALREADY_FINISHED(HttpStatus.BAD_REQUEST, "WALK_400_3", "이미 종료된 산책 기록입니다."),
    WALK_AMOUNT_INVALID(HttpStatus.BAD_REQUEST, "WALK_400_4", "산책 거리는 0 이상 99.9 이하여야 합니다."),
    WALK_FUTURE_DATE(HttpStatus.BAD_REQUEST, "WALK_400_5", "미래 날짜의 산책은 기록할 수 없습니다."),
    WALK_WEATHER_INVALID(HttpStatus.BAD_REQUEST, "WALK_400_6",
            "날씨는 맑음, 흐림, 비, 눈, 바람 중 하나여야 합니다."),
    WALK_STAT_RANGE_TOO_LONG(HttpStatus.BAD_REQUEST, "WALK_400_7", "통계 조회 기간은 최대 366일입니다."),
    WALK_TYPE_INVALID(HttpStatus.BAD_REQUEST, "WALK_400_8",
            "산책 강도는 느긋, 보통, 활발 중 하나여야 합니다."),


    WALK_FORBIDDEN(HttpStatus.FORBIDDEN, "WALK_403_1", "해당 산책 기록에 접근할 권한이 없습니다."),

    WALK_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "WALK_409_1", "이미 진행 중인 산책이 있습니다."),

    WALK_SESSION_CORRUPT(HttpStatus.INTERNAL_SERVER_ERROR, "WALK_500_1", "산책 세션을 처리하지 못했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
