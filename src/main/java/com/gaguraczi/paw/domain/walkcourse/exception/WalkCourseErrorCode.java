package com.gaguraczi.paw.domain.walkcourse.exception;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum WalkCourseErrorCode implements BaseErrorCode {

    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404_1", "존재하지 않는 산책 코스입니다."),
    COURSE_PET_NOT_FOUND(HttpStatus.NOT_FOUND, "COURSE_404_2", "존재하지 않는 반려동물입니다."),

    COURSE_NAME_DUPLICATED(HttpStatus.CONFLICT, "COURSE_409_1", "이미 같은 이름의 코스가 있습니다."),

    COURSE_FORBIDDEN(HttpStatus.FORBIDDEN, "COURSE_403_1", "해당 산책 코스에 접근할 권한이 없습니다."),

    COURSE_PATH_INVALID(HttpStatus.BAD_REQUEST, "COURSE_400_1", "경로 좌표 형식이 올바르지 않습니다."),
    COURSE_DISTANCE_INVALID(HttpStatus.BAD_REQUEST, "COURSE_400_2", "코스 거리는 0 이상 999.9 이하여야 합니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
