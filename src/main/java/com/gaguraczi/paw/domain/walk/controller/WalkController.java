package com.gaguraczi.paw.domain.walk.controller;

import com.gaguraczi.paw.domain.walk.dto.response.*;
import com.gaguraczi.paw.domain.walk.exception.WalkSuccessCode;
import com.gaguraczi.paw.domain.walk.dto.request.WalkCreateRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkFinishRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkStartRequest;
import com.gaguraczi.paw.domain.walk.dto.request.WalkUpdateRequest;
import com.gaguraczi.paw.domain.walk.service.WalkService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/walks")
@RequiredArgsConstructor
@Tag(name = "Walk", description = "산책 기록 API")
public class WalkController {

    private final WalkService walkService;


    @Operation(summary = "산책 수동 기록 저장",
            description = "산책이 끝난 뒤 시간·거리·강도·컨디션을 한 번에 저장합니다.")
    @PostMapping
    public ApiResponse<WalkResponse> createWalk(@Valid @RequestBody WalkCreateRequest request) {
        WalkResponse result = walkService.createWalk(request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_CREATED, result);
    }



    @Operation(summary = "산책 자동기록 시작",
            description = "타이머를 시작합니다. 응답의 walkId를 들고 있다가 종료 API에 넘겨주세요.")
    @PostMapping("/start")
    public ApiResponse<WalkStartResponse> startWalk(@Valid @RequestBody WalkStartRequest request) {
        WalkStartResponse result = walkService.startWalk(request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_STARTED, result);
    }

    @Operation(summary = "산책 자동기록 종료",
            description = "타이머를 종료하고 거리·강도·컨디션을 채워 기록을 완성합니다.")
    @PatchMapping("/{walkId}/finish")
    public ApiResponse<WalkResponse> finishWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId,
            @Valid @RequestBody WalkFinishRequest request) {
        WalkResponse result = walkService.finishWalk(walkId, request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_FINISHED, result);
    }

    @Operation(summary = "진행 중인 산책 조회",
            description = "앱을 껐다 켰을 때 진행 중이던 타이머를 복구하기 위해 사용합니다.")
    @GetMapping("/in-progress")
    public ApiResponse<WalkResponse> getInProgressWalk(
            @Parameter(description = "반려동물 id", example = "1") @RequestParam Long petId) {
        WalkResponse result = walkService.getInProgressWalk(petId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_IN_PROGRESS_FETCHED, result);
    }


    @Operation(summary = "산책 기록 단건 조회")
    @GetMapping("/{walkId}")
    public ApiResponse<WalkResponse> getWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId) {
        WalkResponse result = walkService.getWalk(walkId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_FETCHED, result);
    }

    @Operation(summary = "산책 기록 목록 조회",
            description = "date 를 주면 그 날짜만, startDate·endDate 를 주면 기간별로, 아무것도 안 주면 전체를 조회합니다.")
    @GetMapping
    public ApiResponse<List<WalkSummaryResponse>> getWalks(
            @Parameter(description = "반려동물 id", example = "1")
            @RequestParam Long petId,

            @Parameter(description = "특정 날짜 조회", example = "2026-07-06")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,

            @Parameter(description = "기간 조회 시작일", example = "2026-07-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "기간 조회 종료일", example = "2026-07-31")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<WalkSummaryResponse> result = walkService.getWalks(petId, date, startDate, endDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_LIST_FETCHED, result);
    }


    @Operation(summary = "주간 산책 요약",
            description = "피그마 대시보드 상단 카드용. 이번 주 평균 산책 시간과 지난주 대비 증감을 내려줍니다.")
    @GetMapping("/statistics/weekly")
    public ApiResponse<WalkWeeklySummaryResponse> getWeeklySummary(
            @Parameter(description = "반려동물 id", example = "1")
            @RequestParam Long petId,

            @Parameter(description = "기준 날짜. 안 주면 오늘이 속한 주", example = "2026-07-06")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate baseDate) {

        WalkWeeklySummaryResponse result = walkService.getWeeklySummary(petId, baseDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_WEEKLY_SUMMARY_FETCHED, result);
    }

    @Operation(summary = "일별 산책 통계",
            description = "피그마 '일별 산책 시간' 막대그래프용. 산책을 안 한 날도 0으로 채워서 내려줍니다. "
                    + "날짜를 안 주면 최근 7일입니다.")
    @GetMapping("/statistics/daily")
    public ApiResponse<List<WalkDailyStatResponse>> getDailyStats(
            @Parameter(description = "반려동물 id", example = "1")
            @RequestParam Long petId,

            @Parameter(description = "시작일. 안 주면 종료일 기준 7일 전", example = "2026-07-01")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,

            @Parameter(description = "종료일. 안 주면 오늘", example = "2026-07-07")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        List<WalkDailyStatResponse> result = walkService.getDailyStats(petId, startDate, endDate);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_DAILY_STAT_FETCHED, result);
    }


    @Operation(summary = "산책 기록 수정", description = "보낸 필드만 반영됩니다.")
    @PatchMapping("/{walkId}")
    public ApiResponse<WalkResponse> updateWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId,
            @Valid @RequestBody WalkUpdateRequest request) {
        WalkResponse result = walkService.updateWalk(walkId, request);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_UPDATED, result);
    }

    @Operation(summary = "산책 기록 삭제")
    @DeleteMapping("/{walkId}")
    public ApiResponse<WalkIdResponse> deleteWalk(
            @Parameter(description = "산책 id", example = "1") @PathVariable Long walkId) {
        WalkIdResponse result = walkService.deleteWalk(walkId);
        return ApiResponse.onSuccess(WalkSuccessCode.WALK_DELETED, result);
    }
}
