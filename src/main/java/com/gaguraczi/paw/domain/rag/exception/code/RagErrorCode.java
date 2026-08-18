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
            "rag_document 테이블이 없습니다. rag/sql/rag_document.sql을 적용하거나 dump를 restore 하세요."),
    RAG_VECTOR_STORE_MISSING(HttpStatus.SERVICE_UNAVAILABLE, "RAG_503_1",
            "OpenAI 벡터 저장소 ID가 없습니다. OPENAI_VECTOR_STORE_ID를 설정하세요."),
    RAG_EMBEDDING_FAILED(HttpStatus.BAD_GATEWAY, "RAG_502", "임베딩 요청에 실패했습니다."),
    RAG_SEARCH_FAILED(HttpStatus.BAD_GATEWAY, "RAG_502_1", "지식 질의 요청에 실패했습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
