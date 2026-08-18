package com.gaguraczi.paw.domain.rag.dto.res;

import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "RAG 질의 응답")
public record RagAskRes(
        @Schema(description = "모델이 지식베이스를 조회해 만든 답변")
        String answer,

        @Schema(description = "모델이 참고한 검색 결과")
        List<RagSearchRes> sources
) {

    public static RagAskRes from(RagAskResult result) {
        List<RagSearchRes> sources = result.sources() == null
                ? List.of()
                : result.sources().stream().map(RagSearchRes::from).toList();
        return new RagAskRes(result.answer(), sources);
    }
}
