package com.gaguraczi.paw.domain.weights.controller;

import com.gaguraczi.paw.domain.weights.dto.req.PetWeightCreateReq;
import com.gaguraczi.paw.domain.weights.dto.req.PetWeightUpdateReq;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightGraphRes;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightRes;
import com.gaguraczi.paw.domain.weights.dto.res.PetWeightSummaryRes;
import com.gaguraczi.paw.domain.weights.enums.WeightGraphPeriodEnum;
import com.gaguraczi.paw.domain.weights.exception.code.PetWeightSuccessCode;
import com.gaguraczi.paw.domain.weights.service.PetWeightService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "pet-weights", description = "건강요약 - 체중 API")
@RestController
@RequestMapping("/pets/{petId}/weights")
@RequiredArgsConstructor
public class PetWeightController {

    private final PetWeightService petWeightService;

    @Operation(
            summary = "체중 기록 저장",
            description = "Access Token(JWT) 필수. 본인 소유 펫만 기록할 수 있습니다. 미래 날짜는 기록할 수 없습니다."
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "저장 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_WEIGHT_CREATE_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PET_WEIGHT_CREATE_200",
                                              "message": "체중 기록이 저장되었습니다.",
                                              "result": {
                                                "petWeightId": 1,
                                                "petId": 1,
                                                "weight": 4.20,
                                                "bodyType": "HEALTHY",
                                                "appetiteType": "LOW",
                                                "memoContent": "식사 후 같은 시간대에 측정했어요.",
                                                "recordedAt": "2026-07-06T20:30:00"
                                              }
                                            }
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "유효성 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_WEIGHT_400_1",
                                    value = """
                                            {"isSuccess":false,"code":"PET_WEIGHT_400_1","message":"미래 날짜로는 체중을 기록할 수 없습니다.","result":null}
                                            """
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "펫 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = """
                                            {"isSuccess":false,"code":"PET_404","message":"펫을 찾을 수 없습니다.","result":null}
                                            """
                            )
                    )
            )
    })
    @PostMapping
    public ApiResponse<PetWeightRes> create(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @RequestBody @Valid PetWeightCreateReq request
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_CREATE_200,
                petWeightService.create(petId, request)
        );
    }

    @Operation(summary = "체중 기록 수정", description = "보낸 필드만 반영됩니다.")
    @PutMapping("/{petWeightId}")
    public ApiResponse<PetWeightRes> update(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "체중 기록 ID", example = "1") @PathVariable Long petWeightId,
            @RequestBody(required = false) @Valid PetWeightUpdateReq request
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_UPDATE_200,
                petWeightService.update(petId, petWeightId, request)
        );
    }

    @Operation(summary = "체중 기록 삭제")
    @DeleteMapping("/{petWeightId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "체중 기록 ID", example = "1") @PathVariable Long petWeightId
    ) {
        petWeightService.delete(petId, petWeightId);
        return ApiResponse.onSuccess(PetWeightSuccessCode.PET_WEIGHT_DELETE_200, null);
    }

    @Operation(
            summary = "건강요약 - 체중 상단 카드",
            description = "현재 체중, 이번 달 증감, 급격한 변화 코멘트를 반환합니다."
    )
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_WEIGHT_SUMMARY_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PET_WEIGHT_SUMMARY_200",
                                              "message": "체중 요약 조회에 성공했습니다.",
                                              "result": {
                                                "petId": 1,
                                                "currentWeight": 4.20,
                                                "lastRecordedAt": "2026-07-06T20:30:00",
                                                "monthChange": 0.10,
                                                "abnormal": false,
                                                "comment": "급격한 변화는 없어요"
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    @GetMapping("/summary")
    public ApiResponse<PetWeightSummaryRes> getSummary(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_SUMMARY_200,
                petWeightService.getSummary(petId)
        );
    }

    @Operation(
            summary = "최근 체중 변화 그래프 (날짜별)",
            description = "ONE_MONTH는 일 단위, SIX_MONTHS는 월 단위로 집계해 날짜 오름차순 포인트를 반환합니다."
    )
    @ApiResponses(
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_WEIGHT_GRAPH_200",
                                    value = """
                                            {
                                              "isSuccess": true,
                                              "code": "PET_WEIGHT_GRAPH_200",
                                              "message": "체중 그래프 조회에 성공했습니다.",
                                              "result": {
                                                "period": "ONE_MONTH",
                                                "startDate": "2026-06-07",
                                                "endDate": "2026-07-06",
                                                "minWeight": 3.90,
                                                "maxWeight": 4.20,
                                                "points": [
                                                  {"date": "2026-06-08", "weight": 3.90},
                                                  {"date": "2026-06-15", "weight": 3.95},
                                                  {"date": "2026-07-06", "weight": 4.20}
                                                ]
                                              }
                                            }
                                            """
                            )
                    )
            )
    )
    @GetMapping("/graph")
    public ApiResponse<PetWeightGraphRes> getGraph(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "조회 기간", example = "ONE_MONTH")
            @RequestParam(required = false, defaultValue = "ONE_MONTH") WeightGraphPeriodEnum period
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_GRAPH_200,
                petWeightService.getGraph(petId, period)
        );
    }

    @Operation(
            summary = "월별 체중 기록 목록",
            description = "year/month 미지정 시 이번 달 기록을 최신순으로 반환합니다."
    )
    @GetMapping
    public ApiResponse<List<PetWeightRes>> getMonthlyRecords(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "연도", example = "2026") @RequestParam(required = false) Integer year,
            @Parameter(description = "월", example = "7") @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_LIST_200,
                petWeightService.getMonthlyRecords(petId, year, month)
        );
    }

    @Operation(summary = "체중 기록 상세 조회")
    @GetMapping("/{petWeightId}")
    public ApiResponse<PetWeightRes> get(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "체중 기록 ID", example = "1") @PathVariable Long petWeightId
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_GET_200,
                petWeightService.get(petId, petWeightId)
        );
    }
}
