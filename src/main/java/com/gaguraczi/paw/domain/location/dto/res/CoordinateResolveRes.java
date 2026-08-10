package com.gaguraczi.paw.domain.location.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

@Builder
@Schema(description = "좌표 → 시군구 regionCode + 주소 응답")
public record CoordinateResolveRes(
        @Schema(description = "시군구 LegalRegion 코드", example = "1111000000")
        String regionCode,
        @Schema(description = "시군구 이름", example = "서울특별시 종로구")
        String regionName,
        @Schema(description = "표시용 주소", example = "서울특별시 종로구 세종대로 110")
        String address,
        @Schema(description = "요청 위도", example = "37.5665")
        Double latitude,
        @Schema(description = "요청 경도", example = "126.9780")
        Double longitude
) {
}
