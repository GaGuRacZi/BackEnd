package com.gaguraczi.paw.domain.medication.controller;

import com.gaguraczi.paw.domain.medication.dto.res.MedicationDetailRes;
import com.gaguraczi.paw.domain.medication.dto.res.MedicationSearchRes;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationSuccessCode;
import com.gaguraczi.paw.domain.medication.service.MedicationSearchService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "medications", description = "약물 검색 API")
@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationSearchService medicationSearchService;

    @Operation(
            summary = "약물 검색",
            description = "JWT 필수. 약물명 또는 성분명으로 검색합니다. 이름/성분 ILIKE와 의미 검색을 함께 사용합니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "MEDICATION_SEARCH_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEDICATION_SEARCH_200",
                                              "message": "약물 검색에 성공했습니다.",
                                              "result": [
                                                {
                                                  "medicationId": 1,
                                                  "nameKo": "카미녹스",
                                                  "nameEn": "Carprofen 25mg",
                                                  "ingredient": "카르프로펜"
                                                }
                                              ]
                                            }
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<List<MedicationSearchRes>> search(
            @Parameter(description = "약물명 또는 성분명", example = "카미녹스")
            @RequestParam(required = false) String q,
            @Parameter(description = "최대 결과 수", example = "10")
            @RequestParam(required = false) Integer topK
    ) {
        return ApiResponse.onSuccess(
                MedicationSuccessCode.MEDICATION_SEARCH_200,
                medicationSearchService.search(q, topK).stream()
                        .map(MedicationSearchRes::from)
                        .toList()
        );
    }

    @Operation(summary = "약물 상세", description = "JWT 필수. 약 설명/주의할 점 마크다운을 반환합니다.")
    @GetMapping("/{medicationId}")
    public ApiResponse<MedicationDetailRes> get(
            @Parameter(description = "약물 ID", example = "1")
            @PathVariable Long medicationId
    ) {
        return ApiResponse.onSuccess(
                MedicationSuccessCode.MEDICATION_GET_200,
                MedicationDetailRes.from(medicationSearchService.get(medicationId))
        );
    }
}
