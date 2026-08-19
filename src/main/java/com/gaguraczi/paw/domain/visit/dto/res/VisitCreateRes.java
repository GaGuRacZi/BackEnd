package com.gaguraczi.paw.domain.visit.dto.res;

import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "VisitCreateRes", description = "녹음 업로드 직후 결과. 항상 status=PROCESSING이며 STT는 이 응답 이후 비동기로 진행됩니다.")
public record VisitCreateRes(
        @Schema(description = "진료 ID. 이후 목록/상세/전사문/처방/AI 요약 경로에 사용합니다.", example = "1")
        Long visitId,
        @Schema(description = "요청한 펫 ID", example = "1")
        Long petId,
        @Schema(description = "처리 상태. 업로드 성공 시 항상 PROCESSING.", example = "PROCESSING")
        VisitStatus status
) {
    public static VisitCreateRes from(Visit visit) {
        return new VisitCreateRes(visit.getVisitId(), visit.getPet().getPetId(), visit.getStatus());
    }
}
