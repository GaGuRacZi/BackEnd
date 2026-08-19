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

@Tag(
        name = "medications",
        description = """
                약물 마스터 검색/상세입니다. 진료 처방에서 **source=CATALOG** 로 약을 고를 때 사용합니다.
                검색 결과의 `medicationId`를 `POST /visits/{visitId}/medications` 에 넣습니다.
                마스터에 없는 약은 이 API를 쓰지 않고 진료 처방에 `source=CUSTOM` + `nameKo`로 직접 입력합니다.
                JWT 필수. 본인 펫과 무관하게 마스터 전체를 검색합니다.
                """
)
@RestController
@RequestMapping("/medications")
@RequiredArgsConstructor
public class MedicationController {

    private final MedicationSearchService medicationSearchService;

    @Operation(
            summary = "약물 검색",
            description = """
                    약물명 또는 성분명으로 마스터를 검색합니다. 이름/성분 ILIKE와 의미 검색을 함께 사용합니다.
                    
                    ## Query
                    - `q`: 검색어. 비어 있으면 `MEDICATION_400`
                    - `topK`: 최대 건수. 생략 시 서버 기본값
                    
                    ## 진료 처방과 연결
                    사용자가 목록에서 한 약을 고르면 `medicationId`를 들고
                    `POST /visits/{visitId}/medications` 에 `source=CATALOG` 로 추가합니다.
                    nameKo/nameEn/ingredient는 처방 추가 시 마스터에서 다시 채우므로 화면 표시용입니다.
                    """
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
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "MEDICATION_400. 검색어 없음.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MEDICATION_400",
                                    value = """
                                            {"isSuccess":false,"code":"MEDICATION_400","message":"검색어를 입력해 주세요.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<List<MedicationSearchRes>> search(
            @Parameter(description = "약물명 또는 성분명", example = "카미녹스")
            @RequestParam(required = false) String q,
            @Parameter(description = "최대 결과 수. 생략 시 서버 기본값.", example = "10")
            @RequestParam(required = false) Integer topK
    ) {
        return ApiResponse.onSuccess(
                MedicationSuccessCode.MEDICATION_SEARCH_200,
                medicationSearchService.search(q, topK).stream()
                        .map(MedicationSearchRes::from)
                        .toList()
        );
    }

    @Operation(
            summary = "약물 상세",
            description = """
                    마스터 약의 설명/주의 마크다운입니다.
                    진료 처방 CATALOG에서 `caution`을 생략하면 서버가 `precautionMd`의 첫 줄을 주의사항으로 넣습니다.
                    없는 ID는 `MEDICATION_404`.
                    """
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "MEDICATION_GET_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "성공",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "MEDICATION_GET_200",
                                              "message": "약물 조회에 성공했습니다.",
                                              "result": {
                                                "medicationId": 1,
                                                "nameKo": "카미녹스",
                                                "nameEn": "Carprofen 25mg",
                                                "ingredient": "카르프로펜",
                                                "descriptionMd": "비스테로이드성 소염제입니다.",
                                                "precautionMd": "- 위장 장애가 있으면 수의사와 상담하세요."
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "MEDICATION_404",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "MEDICATION_404",
                                    value = """
                                            {"isSuccess":false,"code":"MEDICATION_404","message":"약물을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = "JWT_401_1",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = """
                                            {"isSuccess":false,"code":"JWT_401_1","message":"token 유효기간이 만료되었습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @GetMapping("/{medicationId}")
    public ApiResponse<MedicationDetailRes> get(
            @Parameter(description = "약물 ID. 검색 결과의 medicationId.", example = "1")
            @PathVariable Long medicationId
    ) {
        return ApiResponse.onSuccess(
                MedicationSuccessCode.MEDICATION_GET_200,
                MedicationDetailRes.from(medicationSearchService.get(medicationId))
        );
    }
}
