package com.gaguraczi.paw.domain.todo.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum TagErrorCode implements BaseErrorCode {
    TAG_CREATE_400_1(HttpStatus.BAD_REQUEST, "TAG_CREATE_400_1", "태그가 생성되지 않았습니다."),
    TAG_LIST_404_1(HttpStatus.NOT_FOUND, "TAG_LIST_404_1", "태그 목록 조회에 실패했습니다."),
    TAG_GET_404_2(HttpStatus.NOT_FOUND, "TAG_GET_404_2", "태그 조회에 실패했습니다."),
    TAG_DELETE_409_1(HttpStatus.CONFLICT, "TAG_DELETE_409_1", "해당 태그를 사용 중인 투두가 있어 삭제할 수 없습니다."),
    TAG_UPDATE_400_2(HttpStatus.BAD_REQUEST, "TAG_UPDATE_400_2", "태그 수정에 실패했습니다."),
    TAG_DELETE_404_1(HttpStatus.NOT_FOUND, "TAG_DELETE_404_1", "태그 삭제에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}