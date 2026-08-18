package com.gaguraczi.paw.domain.rag.dto.res;

import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "RAG 검색 결과")
public record RagSearchRes(
        @Schema(description = "원본 ID", example = "DR-001-0001")
        String sourceId,

        @Schema(description = "청크 번호", example = "0")
        Integer chunkIndex,

        @Schema(description = "출처 유형", example = "QA")
        RagSourceType sourceType,

        @Schema(description = "과목", example = "내과")
        String department,

        @Schema(description = "생애주기", example = "노령견")
        String lifeCycle,

        @Schema(description = "질환", example = "관절염")
        String disease,

        @Schema(description = "제목")
        String title,

        @Schema(description = "검색된 본문")
        String content,

        @Schema(description = "유사도 점수", example = "0.87")
        double score
) {

    public static RagSearchRes from(RagSearchHit hit) {
        return new RagSearchRes(
                hit.sourceId(),
                hit.chunkIndex(),
                hit.sourceType(),
                hit.department(),
                hit.lifeCycle(),
                hit.disease(),
                hit.title(),
                hit.content(),
                hit.score()
        );
    }
}
