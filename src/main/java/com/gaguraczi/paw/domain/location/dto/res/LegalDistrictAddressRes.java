package com.gaguraczi.paw.domain.location.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "법정동 코드 + 주소 (내부 해석용)")
public record LegalDistrictAddressRes(
        @Schema(description = "법정동 코드(10자리)", example = "1111010100")
        String legalDistrictCode,
        @Schema(description = "표시용 주소", example = "서울특별시 종로구 세종대로 110")
        String address
) {
}
