package com.gaguraczi.paw.domain.rag.dto.res;

import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "RAG 검색 결과")
public class RagSearchRes {

    @Schema(description = "원본 ID", example = "DR-001-0001")
    private final String sourceId;

    @Schema(description = "청크 번호", example = "0")
    private final Integer chunkIndex;

    @Schema(description = "출처 유형", example = "QA")
    private final RagSourceType sourceType;

    @Schema(description = "과목", example = "내과")
    private final String department;

    @Schema(description = "생애주기", example = "노령견")
    private final String lifeCycle;

    @Schema(description = "질환", example = "관절염")
    private final String disease;

    @Schema(description = "제목")
    private final String title;

    @Schema(description = "검색된 본문")
    private final String content;

    @Schema(description = "유사도 점수", example = "0.87")
    private final double score;

    public static RagSearchRes from(RagSearchHit hit) {
        return RagSearchRes.builder()
                .sourceId(hit.sourceId())
                .chunkIndex(hit.chunkIndex())
                .sourceType(hit.sourceType())
                .department(hit.department())
                .lifeCycle(hit.lifeCycle())
                .disease(hit.disease())
                .title(hit.title())
                .content(hit.content())
                .score(hit.score())
                .build();
    }
}
