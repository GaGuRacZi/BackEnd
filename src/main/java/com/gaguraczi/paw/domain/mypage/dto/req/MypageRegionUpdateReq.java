package com.gaguraczi.paw.domain.mypage.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "주소 검색 결과 선택 저장 요청")
public record MypageRegionUpdateReq(
        @NotBlank(message = "regionCode는 필수입니다.")
        @Schema(description = "GET /regions/search 결과의 지역 코드", example = "11680", requiredMode = Schema.RequiredMode.REQUIRED)
        String regionCode
) {
}
