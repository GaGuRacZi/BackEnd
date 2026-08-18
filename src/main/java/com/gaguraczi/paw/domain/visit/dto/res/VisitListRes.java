package com.gaguraczi.paw.domain.visit.dto.res;

import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(name = "VisitListRes", description = "펫별 진료 카드. createdAt 내림차순. PROCESSING/FAILED이면 visitName·oneLineSummary는 null입니다.")
public record VisitListRes(
        @Schema(description = "진료 ID", example = "1")
        Long visitId,
        @Schema(description = "진료 시각. 서버 createdAt(업로드 시각)입니다.", example = "2026-08-19T13:00:00")
        LocalDateTime visitedAt,
        @Schema(description = "짧은 요약이 만든 진료명. READY가 아니면 null.", example = "스케일링", nullable = true)
        String visitName,
        @Schema(description = "처리 상태", example = "READY")
        VisitStatus status,
        @Schema(description = "코인 AI 상세 요약이 DONE이면 true. GENERATING/NONE이면 false.", example = "false")
        boolean aiSummaryGenerated,
        @Schema(description = "한 줄 요약. READY가 아니면 null.", example = "치석 제거와 잇몸 관리 안내를 받았어요.", nullable = true)
        String oneLineSummary
) {
    public static VisitListRes from(Visit visit) {
        boolean ready = visit.getStatus() == VisitStatus.READY;
        return new VisitListRes(
                visit.getVisitId(),
                visit.getCreatedAt(),
                ready ? visit.getVisitName() : null,
                visit.getStatus(),
                visit.getAiSummaryStatus() == AiSummaryStatus.DONE,
                ready ? visit.getOneLineSummary() : null
        );
    }
}
