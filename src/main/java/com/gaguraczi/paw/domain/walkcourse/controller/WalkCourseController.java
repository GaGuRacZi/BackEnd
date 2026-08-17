package com.gaguraczi.paw.domain.walkcourse.controller;

import com.gaguraczi.paw.domain.walkcourse.exception.WalkCourseSuccessCode;
import com.gaguraczi.paw.domain.walkcourse.dto.request.WalkCourseCreateRequest;
import com.gaguraczi.paw.domain.walkcourse.dto.request.WalkCourseUpdateRequest;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseIdResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseResponse;
import com.gaguraczi.paw.domain.walkcourse.dto.response.WalkCourseSummaryResponse;
import com.gaguraczi.paw.domain.walkcourse.service.WalkCourseService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequestMapping("/api/walk-courses")
@RequiredArgsConstructor
@Tag(name = "WalkCourse", description = "산책 코스 API")
public class WalkCourseController {

    private final WalkCourseService walkCourseService;

    @Operation(summary = "산책 코스 등록",
            description = "지도에서 고른 코스를 저장합니다. 경로 좌표는 순서대로 보내주세요.")
    @PostMapping
    public ApiResponse<WalkCourseResponse> createCourse(
            @Valid @RequestBody WalkCourseCreateRequest request) {
        WalkCourseResponse result = walkCourseService.createCourse(request);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_CREATED, result);
    }

    @Operation(summary = "자주 걷는 코스 조회",
            description = "피그마 '자주 걷는 코스' 카드용. 사용 횟수가 많은 순으로 내려줍니다.")
    @GetMapping("/frequent")
    public ApiResponse<List<WalkCourseSummaryResponse>> getFrequentCourses(
            @Parameter(description = "반려동물 id", example = "1") @RequestParam Long petId,
            @Parameter(description = "가져올 개수. 기본 3", example = "3")
            @RequestParam(required = false) Integer size) {
        List<WalkCourseSummaryResponse> result = walkCourseService.getFrequentCourses(petId, size);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_FREQUENT_FETCHED, result);
    }

    @Operation(summary = "산책 코스 목록 조회", description = "최근에 걸은 순으로 내려줍니다.")
    @GetMapping
    public ApiResponse<List<WalkCourseSummaryResponse>> getCourses(
            @Parameter(description = "반려동물 id", example = "1") @RequestParam Long petId) {
        List<WalkCourseSummaryResponse> result = walkCourseService.getCourses(petId);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_LIST_FETCHED, result);
    }

    @Operation(summary = "산책 코스 단건 조회", description = "지도에 그릴 경로 좌표까지 함께 내려줍니다.")
    @GetMapping("/{courseId}")
    public ApiResponse<WalkCourseResponse> getCourse(
            @Parameter(description = "코스 id", example = "1") @PathVariable Long courseId) {
        WalkCourseResponse result = walkCourseService.getCourse(courseId);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_FETCHED, result);
    }

    @Operation(summary = "산책 코스 수정", description = "보낸 필드만 반영됩니다.")
    @PatchMapping("/{courseId}")
    public ApiResponse<WalkCourseResponse> updateCourse(
            @Parameter(description = "코스 id", example = "1") @PathVariable Long courseId,
            @Valid @RequestBody WalkCourseUpdateRequest request) {
        WalkCourseResponse result = walkCourseService.updateCourse(courseId, request);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_UPDATED, result);
    }

    @Operation(summary = "산책 코스 삭제")
    @DeleteMapping("/{courseId}")
    public ApiResponse<WalkCourseIdResponse> deleteCourse(
            @Parameter(description = "코스 id", example = "1") @PathVariable Long courseId) {
        WalkCourseIdResponse result = walkCourseService.deleteCourse(courseId);
        return ApiResponse.onSuccess(WalkCourseSuccessCode.COURSE_DELETED, result);
    }
}
