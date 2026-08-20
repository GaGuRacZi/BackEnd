package com.gaguraczi.paw.domain.walk.exception;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;


@Getter
@AllArgsConstructor
public enum WalkSuccessCode implements BaseSuccessCode {

    WALK_CREATED(HttpStatus.CREATED, "WALK_201_1", "산책 기록이 저장되었습니다."),
    WALK_STARTED(HttpStatus.CREATED, "WALK_201_2", "산책을 시작했습니다."),

    WALK_FINISHED(HttpStatus.OK, "WALK_200_1", "산책을 종료했습니다."),
    WALK_FETCHED(HttpStatus.OK, "WALK_200_2", "산책 기록을 조회했습니다."),
    WALK_LIST_FETCHED(HttpStatus.OK, "WALK_200_3", "산책 기록 목록을 조회했습니다."),
    WALK_UPDATED(HttpStatus.OK, "WALK_200_4", "산책 기록이 수정되었습니다."),
    WALK_DELETED(HttpStatus.OK, "WALK_200_5", "산책 기록이 삭제되었습니다."),
    WALK_IN_PROGRESS_FETCHED(HttpStatus.OK, "WALK_200_6", "진행 중인 산책을 조회했습니다."),
    WALK_WEEKLY_SUMMARY_FETCHED(HttpStatus.OK, "WALK_200_7", "주간 산책 요약을 조회했습니다."),
    WALK_DAILY_STAT_FETCHED(HttpStatus.OK, "WALK_200_8", "일별 산책 통계를 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
