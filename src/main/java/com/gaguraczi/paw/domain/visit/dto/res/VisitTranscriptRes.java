package com.gaguraczi.paw.domain.visit.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(
        name = "VisitTranscriptRes",
        description = "의사/보호자 턴으로 나뉜 전사문. status=READY일 때만 조회할 수 있습니다. PROCESSING/FAILED면 VISIT_400_NOT_READY."
)
public record VisitTranscriptRes(
        @Schema(description = "진료 ID", example = "1")
        Long visitId,
        @Schema(description = "짧은 요약이 추출한 병원명. 전사에 없으면 null.", example = "OO동물병원", nullable = true)
        String hospitalName,
        @Schema(description = "진료 시각(업로드 createdAt)", example = "2026-08-19T13:00:00")
        LocalDateTime visitedAt,
        @Schema(description = "녹음 재생 URL", example = "https://cdn.example.com/visit-audio/xxx.m4a")
        String audioUrl,
        @Schema(description = "녹음 길이(초). STT duration이 있으면 그 값, 없으면 업로드 시 측정값.", example = "780")
        Integer durationSec,
        @Schema(description = "시간 순 대화 턴")
        List<VisitTranscriptTurnRes> turns
) {
}
