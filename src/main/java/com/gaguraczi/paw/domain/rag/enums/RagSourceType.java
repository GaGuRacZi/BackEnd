package com.gaguraczi.paw.domain.rag.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "지식 출처 유형. QA=질의응답 문서, CORPUS=본문/코퍼스.")
public enum RagSourceType {
    QA,
    CORPUS
}
