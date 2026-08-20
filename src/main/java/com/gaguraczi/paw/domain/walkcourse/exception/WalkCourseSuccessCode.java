package com.gaguraczi.paw.domain.walkcourse.exception;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum WalkCourseSuccessCode implements BaseSuccessCode {

    COURSE_CREATED(HttpStatus.CREATED, "COURSE_201_1", "산책 코스가 등록되었습니다."),

    COURSE_FETCHED(HttpStatus.OK, "COURSE_200_1", "산책 코스를 조회했습니다."),
    COURSE_LIST_FETCHED(HttpStatus.OK, "COURSE_200_2", "산책 코스 목록을 조회했습니다."),
    COURSE_FREQUENT_FETCHED(HttpStatus.OK, "COURSE_200_3", "자주 걷는 코스를 조회했습니다."),
    COURSE_UPDATED(HttpStatus.OK, "COURSE_200_4", "산책 코스가 수정되었습니다."),
    COURSE_DELETED(HttpStatus.OK, "COURSE_200_5", "산책 코스가 삭제되었습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
