package com.gaguraczi.paw.domain.visit.dto.res;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.gaguraczi.paw.domain.rag.dto.res.RagSearchRes;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(
        name = "VisitAiSummaryRes",
        description = """
                AI 상세 요약 결과. 본문은 공백 포함 약 1000~1500자 한국어 해요체 마크다운입니다.
                coin/usedCoin은 이 요청 이후의 잔액입니다(이미 DONE이면 차감 없음).
                sources는 이번 생성에서 file_search가 찾은 지식 출처입니다. 이미 DONE인 요약을 다시 받으면 빈 배열입니다.
                VISIT_AI_SUMMARY_INCLUDE_SOURCES=false이면 sources 필드 자체가 응답에 없습니다.
                """
)
public record VisitAiSummaryRes(
        @Schema(description = "진료 ID", example = "1")
        Long visitId,
        @Schema(description = "AI 상세 요약 마크다운")
        String aiSummaryMd,
        @Schema(description = "남은 코인", example = "9")
        int coin,
        @Schema(description = "사용한 코인 누적", example = "1")
        int usedCoin,
        @JsonInclude(JsonInclude.Include.NON_NULL)
        @Schema(
                description = "file_search 출처. 환경변수 VISIT_AI_SUMMARY_INCLUDE_SOURCES=false이면 JSON에서 생략. 재조회(이미 DONE)면 빈 배열.",
                nullable = true
        )
        List<RagSearchRes> sources
) {
    public static VisitAiSummaryRes of(Long visitId, String aiSummaryMd, int coin, int usedCoin, List<RagSearchRes> sources) {
        return new VisitAiSummaryRes(
                visitId,
                aiSummaryMd,
                coin,
                usedCoin,
                sources == null ? null : List.copyOf(sources)
        );
    }
}
