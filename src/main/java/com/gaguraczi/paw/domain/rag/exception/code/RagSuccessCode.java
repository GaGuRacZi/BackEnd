package com.gaguraczi.paw.domain.rag.exception.code;

import com.gaguraczi.paw.global.api.code.BaseSuccessCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RagSuccessCode implements BaseSuccessCode {

    RAG_SEARCH_200(HttpStatus.OK, "RAG_SEARCH_200", "지식 질의에 성공했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
