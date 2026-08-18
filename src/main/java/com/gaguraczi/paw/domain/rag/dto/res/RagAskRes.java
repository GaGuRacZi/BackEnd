package com.gaguraczi.paw.domain.rag.dto.res;

import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@Schema(description = "RAG 질의 응답")
public class RagAskRes {

    @Schema(description = "모델이 지식베이스를 조회해 만든 답변")
    private final String answer;

    @Schema(description = "모델이 참고한 검색 결과")
    private final List<RagSearchRes> sources;

    public static RagAskRes from(RagAskResult result) {
        List<RagSearchRes> sources = result.sources() == null
                ? List.of()
                : result.sources().stream().map(RagSearchRes::from).toList();
        return RagAskRes.builder()
                .answer(result.answer())
                .sources(sources)
                .build();
    }
}
