package com.gaguraczi.paw.domain.todo.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TodoErrorCode implements BaseErrorCode {

    TODO_CREATE_400_1(HttpStatus.BAD_REQUEST, "TODO_CREATE_400_1", "투두가 생성되지 않았습니다."),
    TODO_DATE_REQUIRED_400_2(HttpStatus.BAD_REQUEST, "TODO_DATE_REQUIRED_400_2", "일반 투두는 날짜가 필요합니다."),
    TODO_ROUTINE_END_DATE_400_3(HttpStatus.BAD_REQUEST, "TODO_ROUTINE_END_DATE_400_3", "루틴 투두는 종료일이 필요합니다."),
    TODO_ROUTINE_WEEK_400_4(HttpStatus.BAD_REQUEST, "TODO_ROUTINE_WEEK_400_4", "루틴 투두는 반복 요일이 필요합니다."),
    TODO_ROUTINE_RANGE_400_5(HttpStatus.BAD_REQUEST, "TODO_ROUTINE_RANGE_400_5", "루틴 시작일은 종료일보다 늦을 수 없습니다."),
    TODO_ROUTINE_TYPE_CHANGE_400_6(HttpStatus.BAD_REQUEST, "TODO_ROUTINE_TYPE_CHANGE_400_6", "루틴 여부는 변경할 수 없습니다."),
    TODO_UPDATE_400_7(HttpStatus.BAD_REQUEST, "TODO_UPDATE_400_7", "투두 수정에 실패했습니다."),

    TODO_GET_404_1(HttpStatus.NOT_FOUND, "TODO_GET_404_1", "투두를 찾을 수 없습니다."),
    TODO_DATE_GET_404_2(HttpStatus.NOT_FOUND, "TODO_DATE_GET_404_2", "해당 날짜의 투두를 찾을 수 없습니다."),
    TODO_TAG_404_3(HttpStatus.NOT_FOUND, "TODO_TAG_404_3", "태그를 찾을 수 없습니다."),
    TODO_DELETE_404_4(HttpStatus.NOT_FOUND, "TODO_DELETE_404_4", "투두 삭제에 실패했습니다."),

    TODO_ACCESS_403_1(HttpStatus.FORBIDDEN, "TODO_ACCESS_403_1", "해당 투두에 접근할 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}