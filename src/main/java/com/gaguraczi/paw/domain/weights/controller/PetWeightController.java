package com.gaguraczi.paw.domain.weights.controller;

import com.gaguraczi.paw.domain.weights.dto.req.PetWeightCreateMultipart;
import com.gaguraczi.paw.domain.weights.dto.req.PetWeightCreateReq;
import com.gaguraczi.paw.domain.weights.dto.req.PetWeightUpdateMultipart;
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
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "pet-weights", description = PetWeightApiDocs.TAG_DESCRIPTION)
@RestController
@RequestMapping("/pets/{petId}/weights")
@RequiredArgsConstructor
public class PetWeightController {

    private final PetWeightService petWeightService;

    @Operation(
            summary = "체중 기록 저장",
            description = PetWeightApiDocs.CREATE_DESCRIPTION,
            requestBody = @RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PetWeightCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "images", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_CREATE_200",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_CREATE_200", value = PetWeightApiDocs.CREATE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PET_WEIGHT_400_1 미래 날짜 / PET_WEIGHT_400_3 사진 3장 초과 / PET_WEIGHT_400_5 5MB 초과 / PET_WEIGHT_400_6 포맷",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "PET_WEIGHT_400_1", value = PetWeightApiDocs.PET_WEIGHT_400_1_EXAMPLE),
                            @ExampleObject(name = "PET_WEIGHT_400_3", value = PetWeightApiDocs.PET_WEIGHT_400_3_EXAMPLE),
                            @ExampleObject(name = "PET_WEIGHT_400_5", value = PetWeightApiDocs.PET_WEIGHT_400_5_EXAMPLE),
                            @ExampleObject(name = "PET_WEIGHT_400_6", value = PetWeightApiDocs.PET_WEIGHT_400_6_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404. 없거나 본인 펫이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_404", value = PetWeightApiDocs.PET_404_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = PetWeightApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = PetWeightApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetWeightRes> create(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @RequestPart("data") @Valid PetWeightCreateReq request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_CREATE_200,
                petWeightService.create(petId, request, images)
        );
    }

    @Operation(
            summary = "체중 기록 수정",
            description = PetWeightApiDocs.UPDATE_DESCRIPTION,
            requestBody = @RequestBody(
                    required = false,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = PetWeightUpdateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "images", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_UPDATE_200",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_UPDATE_200", value = PetWeightApiDocs.UPDATE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PET_WEIGHT_400_3 사진 3장 초과 / PET_WEIGHT_400_1 미래 날짜",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "PET_WEIGHT_400_3", value = PetWeightApiDocs.PET_WEIGHT_400_3_EXAMPLE),
                            @ExampleObject(name = "PET_WEIGHT_400_1", value = PetWeightApiDocs.PET_WEIGHT_400_1_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_WEIGHT_404 기록 없음 / PET_404 펫 없음",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "PET_WEIGHT_404", value = PetWeightApiDocs.PET_WEIGHT_404_EXAMPLE),
                            @ExampleObject(name = "PET_404", value = PetWeightApiDocs.PET_404_EXAMPLE)
                    })
            )
    })
    @PutMapping(value = "/{petWeightId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<PetWeightRes> update(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "체중 기록 ID", example = "1") @PathVariable Long petWeightId,
            @RequestPart(value = "data", required = false) @Valid PetWeightUpdateReq request,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_UPDATE_200,
                petWeightService.update(petId, petWeightId, request, images)
        );
    }

    @Operation(summary = "체중 기록 삭제", description = PetWeightApiDocs.DELETE_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_DELETE_200. result=null.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_DELETE_200", value = PetWeightApiDocs.DELETE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_WEIGHT_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_404", value = PetWeightApiDocs.PET_WEIGHT_404_EXAMPLE))
            )
    })
    @DeleteMapping("/{petWeightId}")
    public ApiResponse<Void> delete(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "체중 기록 ID", example = "1") @PathVariable Long petWeightId
    ) {
        petWeightService.delete(petId, petWeightId);
        return ApiResponse.onSuccess(PetWeightSuccessCode.PET_WEIGHT_DELETE_200, null);
    }

    @Operation(summary = "건강요약 - 체중 상단 카드", description = PetWeightApiDocs.SUMMARY_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_SUMMARY_200. monthChange는 비교 대상이 없으면 null.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_SUMMARY_200", value = PetWeightApiDocs.SUMMARY_200_EXAMPLE))
            )
    })
    @GetMapping("/summary")
    public ApiResponse<PetWeightSummaryRes> getSummary(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_SUMMARY_200,
                petWeightService.getSummary(petId)
        );
    }

    @Operation(summary = "최근 체중 변화 그래프 (날짜별)", description = PetWeightApiDocs.GRAPH_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_GRAPH_200. 기록 없는 날짜는 포인트에 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_GRAPH_200", value = PetWeightApiDocs.GRAPH_200_EXAMPLE))
            )
    })
    @GetMapping("/graph")
    public ApiResponse<PetWeightGraphRes> getGraph(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "ONE_MONTH=최근 1개월 일 단위, SIX_MONTHS=최근 6개월 월 단위", example = "ONE_MONTH")
            @RequestParam(required = false, defaultValue = "ONE_MONTH") WeightGraphPeriodEnum period
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_GRAPH_200,
                petWeightService.getGraph(petId, period)
        );
    }

    @Operation(summary = "월별 체중 기록 목록", description = PetWeightApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_LIST_200. 최신 recordedAt 순.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_LIST_200", value = PetWeightApiDocs.LIST_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "PET_WEIGHT_400_2. month가 1~12가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_400_2", value = PetWeightApiDocs.PET_WEIGHT_400_2_EXAMPLE))
            )
    })
    @GetMapping
    public ApiResponse<List<PetWeightRes>> getMonthlyRecords(
            @Parameter(description = "펫 ID", example = "1") @PathVariable Long petId,
            @Parameter(description = "연도. month와 함께 지정. 생략 시 이번 달", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "월(1~12). year와 함께 지정. 생략 시 이번 달", example = "7")
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                PetWeightSuccessCode.PET_WEIGHT_LIST_200,
                petWeightService.getMonthlyRecords(petId, year, month)
        );
    }

    @Operation(summary = "체중 기록 상세 조회", description = PetWeightApiDocs.GET_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "PET_WEIGHT_GET_200",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_GET_200", value = PetWeightApiDocs.GET_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_WEIGHT_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_WEIGHT_404", value = PetWeightApiDocs.PET_WEIGHT_404_EXAMPLE))
            )
    })
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
