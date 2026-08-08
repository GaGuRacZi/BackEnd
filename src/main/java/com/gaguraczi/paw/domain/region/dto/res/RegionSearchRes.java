package com.gaguraczi.paw.domain.region.dto.res;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(description = "시/군/구 지역 검색 응답")
public record RegionSearchRes(
        @Schema(description = "시군구 LegalRegion 코드", example = "1111000000")
        String code,
        @Schema(description = "시군구 이름", example = "서울특별시 종로구")
        String name,
        @Schema(description = "하위 동 미리보기 (일부)", example = "[\"청운효자동\", \"사직동\", \"삼청동\"]")
        List<String> dongPreview
) {
    public static RegionSearchRes of(String code, String name, List<String> dongPreview) {
        return new RegionSearchRes(code, name, dongPreview);
    }
}
