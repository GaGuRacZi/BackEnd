package com.gaguraczi.paw.domain.location.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "표시용 주소 응답")
public record AddressRes(
        @Schema(description = "도로명/지번 표시 주소", example = "서울특별시 종로구 세종대로 110")
        String address
) {
}
