package com.gaguraczi.paw.domain.visit.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(name = "VisitCreateReq", description = "진료 녹음 업로드의 data 파트 JSON. 진료명·병원명은 보내지 않으며 STT 이후 서버가 채웁니다.")
public record VisitCreateReq(
        @NotNull
        @Schema(description = "본인 소유 펫 ID. 없거나 다른 사람 펫이면 PET_404.", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
        Long petId
) {
}
