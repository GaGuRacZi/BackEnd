package com.gaguraczi.paw.domain.todo.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TagSuccessCode implements BaseSuccessCode {


    TAG_CREATE_201(HttpStatus.CREATED, "TAG_CREATE_201", "태그가 생성되었습니다."),
    TAG_LIST_200(HttpStatus.OK, "TAG_LIST_200", "태그 목록을 조회했습니다."),
    TAG_GET_200(HttpStatus.OK, "TAG_GET_200", "태그를 조회했습니다."),
    TAG_UPDATE_200(HttpStatus.OK, "TAG_UPDATE_200", "태그를 수정했습니다."),
    TAG_DELETE_200(HttpStatus.OK, "TAG_DELETE_200", "태그를 삭제했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}