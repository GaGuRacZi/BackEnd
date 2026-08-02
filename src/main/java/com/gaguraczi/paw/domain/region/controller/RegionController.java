package com.gaguraczi.paw.domain.region.controller;

import com.gaguraczi.paw.domain.region.dto.res.RegionSearchRes;
import com.gaguraczi.paw.domain.region.exception.code.RegionSuccessCode;
import com.gaguraczi.paw.domain.region.service.LegalRegionService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
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

    @Operation(summary = "시/군/구 지역 검색")
    @GetMapping("/search")
    public ApiResponse<List<RegionSearchRes>> search(@RequestParam String q) {
        return ApiResponse.onSuccess(RegionSuccessCode.REGION_SEARCH_200, legalRegionService.search(q));
    }
}
