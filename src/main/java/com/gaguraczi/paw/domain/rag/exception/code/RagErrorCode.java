package com.gaguraczi.paw.domain.rag.exception.code;

import com.gaguraczi.paw.global.api.code.BaseErrorCode;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum RagErrorCode implements BaseErrorCode {

    RAG_QUERY_REQUIRED(HttpStatus.BAD_REQUEST, "RAG_400", "검색어를 입력해 주세요."),
    RAG_CORPUS_PATH_INVALID(HttpStatus.BAD_REQUEST, "RAG_400_1", "말뭉치 경로를 찾을 수 없습니다."),
    RAG_TABLE_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "RAG_503",
            "지식 검색 서비스를 일시적으로 사용할 수 없습니다."),
    RAG_VECTOR_STORE_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "RAG_503_1",
            "지식 검색 서비스를 일시적으로 사용할 수 없습니다."),
    RAG_EMBEDDING_FAILED(HttpStatus.BAD_GATEWAY, "RAG_502", "임베딩 요청에 실패했습니다."),
    RAG_SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "RAG_502_1", "지식 질의 요청에 실패했습니다."),
    RAG_RATE_LIMITED(HttpStatus.TOO_MANY_REQUESTS, "RAG_429", "지식 질의 요청이 너무 많습니다. 잠시 후 다시 시도해 주세요."),
    RAG_SEARCH_UNAVAILABLE(HttpStatus.BAD_GATEWAY, "RAG_502_2", "지식 질의 서버 오류가 반복되어 중단했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
