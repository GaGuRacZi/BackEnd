package com.gaguraczi.paw.domain.visit.dto.res;

import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VisitTranscriptTurnRes", description = "전사문 한 턴. 같은 화자가 이어지면 서버에서 한 턴으로 합쳐질 수 있습니다.")
public record VisitTranscriptTurnRes(
        @Schema(description = "화자. VET=수의사, OWNER=보호자.", example = "VET")
        TranscriptSpeaker speaker,
        @Schema(description = "해당 구간의 말", example = "오늘은 스케일링을 진행했어요.")
        String text,
        @Schema(description = "시작 초. 알 수 없으면 null.", example = "12.4", nullable = true)
        Double startSec,
        @Schema(description = "종료 초. 알 수 없으면 null.", example = "18.1", nullable = true)
        Double endSec
) {
}
