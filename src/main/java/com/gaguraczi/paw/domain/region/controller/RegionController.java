package com.gaguraczi.paw.domain.region.controller;

import com.gaguraczi.paw.domain.region.dto.res.RegionSearchRes;
import com.gaguraczi.paw.domain.region.exception.code.RegionSuccessCode;
import com.gaguraczi.paw.domain.region.service.LegalRegionService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "regions", description = "법정동/지역 API")
@RestController
@RequestMapping("/regions")
@RequiredArgsConstructor
public class RegionController {

    private final LegalRegionService legalRegionService;

    @Operation(
            summary = "시/군/구 지역 검색",
            description = "인증 불필요(permitAll). 시·군·구 단위 LegalRegion을 이름/코드로 검색합니다. q 필수."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "REGION_SEARCH_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "REGION_SEARCH_200",
                                              "message": "지역 검색에 성공했습니다.",
                                              "result": [
                                                {
                                                  "code": "1111000000",
                                                  "name": "서울특별시 종로구",
                                                  "dongPreview": ["청운효자동", "사직동", "삼청동"]
                                                },
                                                {
                                                  "code": "1168000000",
                                                  "name": "서울특별시 강남구",
                                                  "dongPreview": ["역삼동", "삼성동", "청담동"]
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "REGION_400_3",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "검색어 누락",
                                    value = """
                                            {
                                              "isSuccess": false,
                                              "code": "REGION_400_3",
                                              "message": "검색어를 입력해 주세요.",
                                              "result": null
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping("/search")
    public ApiResponse<List<RegionSearchRes>> search(
            @Parameter(description = "지역 검색어 (시/군/구 이름 또는 코드)", example = "종로", required = true)
            @RequestParam String q
    ) {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_SEARCH_200, legalRegionService.search(q));
    }
}
