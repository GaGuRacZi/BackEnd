package com.gaguraczi.paw.domain.walk.controller;

import com.gaguraczi.paw.domain.walk.dto.response.WalkDailyStatResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkIdResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkStartResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkSummaryResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkWeeklySummaryResponse;
import com.gaguraczi.paw.domain.walk.exception.WalkSuccessCode;
import com.gaguraczi.paw.domain.walk.dto.request.WalkCreateRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkFinishRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkStartRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkUpdateRequest;
import com.gaguraczi.paw.domain.walk.service.WalkService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/walks")
@RequiredArgsConstructor
@Tag(name = "Walk", description = WalkApiDocs.TAG_DESCRIPTION)
public class WalkController {

    private final WalkService walkService;

    @Operation(
            summary = "산책 수동 기록 저장",
            description = WalkApiDocs.CREATE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(name = "수동 기록", value = WalkApiDocs.CREATE_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "WALK_201_1. 저장 성공.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_201_1", value = WalkApiDocs.CREATE_201_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "WALK_400_1 시간 역전 / WALK_400_5 미래 날짜 / WALK_400_6 날씨 / WALK_400_8 강도",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "WALK_400_1", value = WalkApiDocs.WALK_400_1_EXAMPLE),
                            @ExampleObject(name = "WALK_400_5", value = WalkApiDocs.WALK_400_5_EXAMPLE),
                            @ExampleObject(name = "WALK_400_6", value = WalkApiDocs.WALK_400_6_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_2. 없거나 본인 펫이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_2", value = WalkApiDocs.WALK_404_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = WalkApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = WalkApiDocs.JWT_401_1_EXAMPLE))
            )
    })
    @PostMapping
    public ApiResponse<WalkResponse> createWalk(@Valid @RequestBody WalkCreateRequest request) {
        WalkResponse result = walkService.createWalk(request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_CREATED, result);
    }

    @Operation(summary = "산책 자동기록 시작", description = WalkApiDocs.START_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "201",
                    description = "WALK_201_2. 타이머 시작. walkId 없음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_201_2", value = WalkApiDocs.START_201_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "409",
                    description = "WALK_409_1. 이미 진행 중인 타이머가 있음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_409_1", value = WalkApiDocs.WALK_409_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_2. 없거나 본인 펫이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_2", value = WalkApiDocs.WALK_404_2_EXAMPLE))
            )
    })
    @PostMapping("/start")
    public ApiResponse<WalkStartResponse> startWalk(@Valid @RequestBody WalkStartRequest request) {
        WalkStartResponse result = walkService.startWalk(request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_STARTED, result);
    }

    @Operation(
            summary = "산책 자동기록 종료",
            description = WalkApiDocs.FINISH_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(name = "타이머 종료", value = WalkApiDocs.FINISH_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_1. DB에 완료 기록 저장.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_1", value = WalkApiDocs.FINISH_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_3. 세션 없음/만료. 새로 start 해야 함.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_3", value = WalkApiDocs.WALK_404_3_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "WALK_400_1. 종료 시각이 시작보다 빠름.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_400_1", value = WalkApiDocs.WALK_400_1_EXAMPLE))
            )
    })
    @PatchMapping("/finish")
    public ApiResponse<WalkResponse> finishWalk(@Valid @RequestBody WalkFinishRequest request) {
        WalkResponse result = walkService.finishWalk(request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_FINISHED, result);
    }

    @Operation(summary = "진행 중인 산책 조회", description = WalkApiDocs.IN_PROGRESS_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_6. walkId=null, walkStatus=IN_PROGRESS.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_6", value = WalkApiDocs.IN_PROGRESS_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_3. 진행 중 세션 없음(만료 포함).",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_3", value = WalkApiDocs.WALK_404_3_EXAMPLE))
            )
    })
    @GetMapping("/in-progress")
    public ApiResponse<WalkResponse> getInProgressWalk(
            @Parameter(description = "반려동물 id", example = "1", required = true)
            @RequestParam Long petId) {
        WalkResponse result = walkService.getInProgressWalk(petId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_IN_PROGRESS_FETCHED, result);
    }

    @Operation(summary = "산책 기록 단건 조회", description = WalkApiDocs.GET_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_2",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_2", value = WalkApiDocs.GET_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_1. 없는 기록.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_1", value = WalkApiDocs.WALK_404_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "WALK_403_1. 본인 기록이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_403_1", value = WalkApiDocs.WALK_403_1_EXAMPLE))
            )
    })
    @GetMapping("/{walkId}")
    public ApiResponse<WalkResponse> getWalk(
            @Parameter(description = "산책 id (완료 기록만)", example = "1") @PathVariable Long walkId) {
        WalkResponse result = walkService.getWalk(walkId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_FETCHED, result);
    }

    @Operation(summary = "산책 기록 목록 조회", description = WalkApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_3. 완료 기록만, 최신순.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_3", value = WalkApiDocs.LIST_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "WALK_400_2. 기간 한쪽만 보냈거나 시작일이 종료일보다 늦음.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_400_2", value = WalkApiDocs.WALK_400_2_EXAMPLE))
            )
    })
    @GetMapping
    public ApiResponse<List<WalkSummaryResponse>> getWalks(
            @Parameter(description = "반려동물 id", example = "1", required = true)
            @RequestParam Long petId,

            @Parameter(description = "특정 날짜만 조회 (yyyy-MM-dd). 있으면 startDate/endDate는 무시", example = "2026-07-06")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "기간 시작일. endDate와 함께 사용", example = "2026-07-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "기간 종료일. startDate와 함께 사용", example = "2026-07-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<WalkSummaryResponse> result = walkService.getWalks(petId, date, startDate, endDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_LIST_FETCHED, result);
    }

    @Operation(summary = "주간 산책 요약", description = WalkApiDocs.WEEKLY_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_7. 월요일~일요일 카드.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_7", value = WalkApiDocs.WEEKLY_200_EXAMPLE))
            )
    })
    @GetMapping("/statistics/weekly")
    public ApiResponse<WalkWeeklySummaryResponse> getWeeklySummary(
            @Parameter(description = "반려동물 id", example = "1", required = true)
            @RequestParam Long petId,

            @Parameter(description = "이 날짜가 속한 주(월~일)를 조회. 생략 시 오늘", example = "2026-07-06")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate) {

        WalkWeeklySummaryResponse result = walkService.getWeeklySummary(petId, baseDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_WEEKLY_SUMMARY_FETCHED, result);
    }

    @Operation(summary = "일별 산책 통계", description = WalkApiDocs.DAILY_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_8. 안 한 날도 0으로 채워 반환.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_8", value = WalkApiDocs.DAILY_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "WALK_400_2 날짜 역전 / WALK_400_7 366일 초과",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "WALK_400_2", value = WalkApiDocs.WALK_400_2_EXAMPLE),
                            @ExampleObject(name = "WALK_400_7", value = WalkApiDocs.WALK_400_7_EXAMPLE)
                    })
            )
    })
    @GetMapping("/statistics/daily")
    public ApiResponse<List<WalkDailyStatResponse>> getDailyStats(
            @Parameter(description = "반려동물 id", example = "1", required = true)
            @RequestParam Long petId,

            @Parameter(description = "시작일. 생략 시 종료일 기준 6일 전(총 7일)", example = "2026-07-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "종료일. 생략 시 오늘", example = "2026-07-07")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<WalkDailyStatResponse> result = walkService.getDailyStats(petId, startDate, endDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_DAILY_STAT_FETCHED, result);
    }

    @Operation(
            summary = "산책 기록 수정",
            description = WalkApiDocs.UPDATE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(name = "부분 수정", value = WalkApiDocs.UPDATE_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_4",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_4", value = WalkApiDocs.UPDATE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_1",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_1", value = WalkApiDocs.WALK_404_1_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "WALK_403_1",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_403_1", value = WalkApiDocs.WALK_403_1_EXAMPLE))
            )
    })
    @PatchMapping("/{walkId}")
    public ApiResponse<WalkResponse> updateWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId,
            @Valid @RequestBody WalkUpdateRequest request) {
        WalkResponse result = walkService.updateWalk(walkId, request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_UPDATED, result);
    }

    @Operation(summary = "산책 기록 삭제", description = WalkApiDocs.DELETE_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "WALK_200_5",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_200_5", value = WalkApiDocs.DELETE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "WALK_404_1",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "WALK_404_1", value = WalkApiDocs.WALK_404_1_EXAMPLE))
            )
    })
    @DeleteMapping("/{walkId}")
    public ApiResponse<WalkIdResponse> deleteWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId) {
        WalkIdResponse result = walkService.deleteWalk(walkId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_DELETED, result);
    }
}
