package com.gaguraczi.paw.domain.pets.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(description = "반려동물별 원료/수술이력/관리부위 선택 저장 요청 (전체 갈아끼우기)")
public record PetCareSelectionReq(
        @NotNull(message = "codeIds는 필수입니다.")
        @Schema(description = "선택할 코드 ID 목록 (전체 대체, 빈 배열이면 전체 해제)", example = "[1, 2, 3]")
        List<Long> codeIds
) {
}
