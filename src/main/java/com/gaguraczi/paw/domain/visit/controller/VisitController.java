package com.gaguraczi.paw.domain.visit.controller;

import com.gaguraczi.paw.domain.visit.dto.req.VisitCreateReq;
import com.gaguraczi.paw.domain.visit.dto.req.VisitPrescriptionAddReq;
import com.gaguraczi.paw.domain.visit.dto.res.VisitAiSummaryRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitCreateRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitDetailRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitListRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitPrescriptionRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitTranscriptRes;
import com.gaguraczi.paw.domain.visit.exception.code.VisitSuccessCode;
import com.gaguraczi.paw.domain.visit.service.VisitService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Encoding;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(
        name = "visits",
        description = VisitApiDocs.TAG_DESCRIPTION
)
@RestController
@RequestMapping("/visits")
@RequiredArgsConstructor
public class VisitController {

    private final VisitService visitService;

    @Operation(
            summary = "진료 녹음 업로드",
            description = VisitApiDocs.CREATE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.MULTIPART_FORM_DATA_VALUE,
                            schema = @Schema(implementation = VisitCreateMultipart.class),
                            encoding = {
                                    @Encoding(name = "data", contentType = MediaType.APPLICATION_JSON_VALUE),
                                    @Encoding(name = "audio", contentType = MediaType.APPLICATION_OCTET_STREAM_VALUE)
                            },
                            examples = @ExampleObject(
                                    name = "data JSON",
                                    value = VisitApiDocs.CREATE_DATA_EXAMPLE
                            )
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_CREATE_200. 등록됨. status는 항상 PROCESSING.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_CREATE_200",
                                    value = VisitApiDocs.CREATE_200_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = VisitApiDocs.CREATE_400_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_400",
                                            value = VisitApiDocs.VISIT_400_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_PET",
                                            value = VisitApiDocs.VISIT_400_PET_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_TYPE",
                                            value = VisitApiDocs.VISIT_400_AUDIO_TYPE_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_TOO_LARGE",
                                            value = VisitApiDocs.VISIT_400_AUDIO_TOO_LARGE_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_AUDIO_DURATION",
                                            value = VisitApiDocs.VISIT_400_AUDIO_DURATION_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404. 펫이 없거나 본인 소유가 아님.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = VisitApiDocs.PET_404_EXAMPLE
                            )
                    )
            )
    })
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<VisitCreateRes> create(
            @RequestPart("data") @Valid VisitCreateReq data,
            @RequestPart("audio") MultipartFile audio
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_CREATE_200, visitService.create(data, audio));
    }

    @Operation(
            summary = "진료 목록",
            description = VisitApiDocs.LIST_DESCRIPTION
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_LIST_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "READY와 PROCESSING 혼재",
                                            value = VisitApiDocs.LIST_200_MIXED_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "FAILED 카드",
                                            value = VisitApiDocs.LIST_200_FAILED_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "PET_404",
                                    value = VisitApiDocs.PET_404_EXAMPLE
                            )
                    )
            )
    })
    @GetMapping
    public ApiResponse<List<VisitListRes>> list(
            @Parameter(description = "본인 펫 ID", example = "1", required = true)
            @RequestParam Long petId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_LIST_200, visitService.list(petId));
    }

    @Operation(
            summary = "진료 요약 상세",
            description = VisitApiDocs.GET_DESCRIPTION
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_GET_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "READY",
                                            value = VisitApiDocs.GET_200_READY_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "PROCESSING",
                                            value = VisitApiDocs.GET_200_PROCESSING_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "FAILED",
                                            value = VisitApiDocs.GET_200_FAILED_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = VisitApiDocs.VISIT_404_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = VisitApiDocs.VISIT_404_EXAMPLE
                            )
                    )
            )
    })
    @GetMapping("/{visitId}")
    public ApiResponse<VisitDetailRes> get(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_GET_200, visitService.get(visitId));
    }

    @Operation(
            summary = "음성 전사문",
            description = VisitApiDocs.TRANSCRIPT_DESCRIPTION
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_TRANSCRIPT_200",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_TRANSCRIPT_200",
                                    value = VisitApiDocs.TRANSCRIPT_200_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY. 아직 PROCESSING이거나 FAILED.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_400_NOT_READY",
                                    value = VisitApiDocs.VISIT_400_NOT_READY_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = VisitApiDocs.VISIT_404_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = VisitApiDocs.VISIT_404_EXAMPLE
                            )
                    )
            )
    })
    @GetMapping("/{visitId}/transcript")
    public ApiResponse<VisitTranscriptRes> transcript(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_TRANSCRIPT_200, visitService.transcript(visitId));
    }

    @Operation(
            summary = "처방 약물 추가",
            description = VisitApiDocs.ADD_PRESCRIPTION_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = VisitPrescriptionAddReq.class),
                            examples = {
                                    @ExampleObject(
                                            name = "CATALOG",
                                            value = VisitApiDocs.ADD_PRESCRIPTION_CATALOG_REQ
                                    ),
                                    @ExampleObject(
                                            name = "CUSTOM",
                                            value = VisitApiDocs.ADD_PRESCRIPTION_CUSTOM_REQ
                                    )
                            }
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_PRESCRIPTION_ADD_200. 추가된 한 건을 반환합니다. 목록 전체는 GET 상세의 prescriptions.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "CATALOG 추가",
                                            value = VisitApiDocs.ADD_PRESCRIPTION_CATALOG_200
                                    ),
                                    @ExampleObject(
                                            name = "CUSTOM 추가",
                                            value = VisitApiDocs.ADD_PRESCRIPTION_CUSTOM_200
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY 또는 VISIT_400_PRESCRIPTION (source/frequency/mealTiming 누락, CATALOG인데 medicationId 없음, CUSTOM인데 nameKo 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_400_NOT_READY",
                                            value = VisitApiDocs.VISIT_400_NOT_READY_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "VISIT_400_PRESCRIPTION",
                                            value = VisitApiDocs.VISIT_400_PRESCRIPTION_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "VISIT_404 또는 MEDICATION_404(CATALOG 마스터 없음)",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "VISIT_404",
                                            value = VisitApiDocs.VISIT_404_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "MEDICATION_404",
                                            value = VisitApiDocs.MEDICATION_404_EXAMPLE
                                    )
                            }
                    )
            )
    })
    @PostMapping("/{visitId}/medications")
    public ApiResponse<VisitPrescriptionRes> addPrescription(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId,
            @RequestBody @Valid VisitPrescriptionAddReq req
    ) {
        return ApiResponse.onSuccess(
                VisitSuccessCode.VISIT_PRESCRIPTION_ADD_200,
                visitService.addPrescription(visitId, req)
        );
    }

    @Operation(
            summary = "처방 약물 삭제",
            description = VisitApiDocs.DELETE_PRESCRIPTION_DESCRIPTION
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_PRESCRIPTION_DELETE_200. result는 null.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_PRESCRIPTION_DELETE_200",
                                    value = VisitApiDocs.DELETE_PRESCRIPTION_200_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = VisitApiDocs.VISIT_404_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = VisitApiDocs.VISIT_404_EXAMPLE
                            )
                    )
            )
    })
    @DeleteMapping("/{visitId}/medications/{prescriptionId}")
    public ApiResponse<Void> deletePrescription(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId,
            @Parameter(description = "처방 ID (추가 응답의 prescriptionId)", example = "10", required = true)
            @PathVariable Long prescriptionId
    ) {
        visitService.deletePrescription(visitId, prescriptionId);
        return ApiResponse.onSuccess(VisitSuccessCode.VISIT_PRESCRIPTION_DELETE_200, null);
    }

    @Operation(
            summary = "AI 상세 요약 생성",
            description = VisitApiDocs.AI_SUMMARY_DESCRIPTION
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "VISIT_AI_SUMMARY_200. coin/usedCoin은 이 요청 이후 잔액.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = {
                                    @ExampleObject(
                                            name = "첫 생성 (sources 포함)",
                                            value = VisitApiDocs.AI_SUMMARY_200_CREATED_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "이미 DONE (재과금 없음, sources 빈 배열)",
                                            value = VisitApiDocs.AI_SUMMARY_200_DONE_EXAMPLE
                                    ),
                                    @ExampleObject(
                                            name = "sources 숨김 설정",
                                            description = "VISIT_AI_SUMMARY_INCLUDE_SOURCES=false 이면 sources 키 없음",
                                            value = VisitApiDocs.AI_SUMMARY_200_HIDDEN_SOURCES_EXAMPLE
                                    )
                            }
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "VISIT_400_NOT_READY",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_400_NOT_READY",
                                    value = VisitApiDocs.VISIT_400_NOT_READY_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = VisitApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_401_1",
                                    value = VisitApiDocs.JWT_401_1_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "402",
                    description = "VISIT_402_COIN. HTTP 402. 잔액 부족.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_402_COIN",
                                    value = VisitApiDocs.VISIT_402_COIN_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = VisitApiDocs.JWT_403_2_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "JWT_403_2",
                                    value = VisitApiDocs.JWT_403_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = VisitApiDocs.VISIT_404_DESCRIPTION,
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_404",
                                    value = VisitApiDocs.VISIT_404_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "VISIT_409. 같은 진료의 AI 요약이 이미 GENERATING.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_409",
                                    value = VisitApiDocs.VISIT_409_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "502",
                    description = "VISIT_502_2 생성 실패(코인 환불됨). 글자 수 범위 미달 등.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_502_2",
                                    value = VisitApiDocs.VISIT_502_2_EXAMPLE
                            )
                    )
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "503",
                    description = "VISIT_503. 지식 검색 벡터스토어 미설정.",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "VISIT_503",
                                    value = VisitApiDocs.VISIT_503_EXAMPLE
                            )
                    )
            )
    })
    @PostMapping("/{visitId}/ai-summary")
    public ApiResponse<VisitAiSummaryRes> generateAiSummary(
            @Parameter(description = "진료 ID", example = "1", required = true)
            @PathVariable Long visitId
    ) {
        return ApiResponse.onSuccess(
                VisitSuccessCode.VISIT_AI_SUMMARY_200,
                visitService.generateAiSummary(visitId)
        );
    }

    @Schema(name = "VisitCreateMultipart", description = "진료 녹음 업로드 multipart. data는 JSON 문자열, audio는 파일입니다.")
    public static class VisitCreateMultipart {
        @Schema(
                description = "JSON. petId만 필요합니다. 예: {\"petId\":1}",
                implementation = VisitCreateReq.class,
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        public VisitCreateReq data;

        @Schema(
                description = "진료 음성. mp3/m4a/aac, 최대 100MB·60분. wav 불가.",
                type = "string",
                format = "binary",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        public MultipartFile audio;
    }
}
