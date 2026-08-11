package com.gaguraczi.paw.domain.todo.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TodoSuccessCode implements BaseSuccessCode {

    TODO_CREATE_201(HttpStatus.CREATED, "TODO_CREATE_201", "투두가 생성되었습니다."),
    TODO_LIST_200(HttpStatus.OK, "TODO_LIST_200", "투두 목록을 조회했습니다."),
    TODO_GET_200(HttpStatus.OK, "TODO_GET_200", "투두를 조회했습니다."),
    TODO_UPDATE_200(HttpStatus.OK, "TODO_UPDATE_200", "투두를 수정했습니다."),
    TODO_COMPLETE_200(HttpStatus.OK, "TODO_COMPLETE_200", "투두 완료 상태를 변경했습니다."),
    TODO_CALENDAR_200(HttpStatus.OK, "TODO_CALENDAR_200", "월별 캘린더를 조회했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}