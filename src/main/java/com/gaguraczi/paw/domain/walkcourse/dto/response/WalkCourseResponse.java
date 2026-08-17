package com.gaguraczi.paw.domain.walkcourse.dto.response;

import com.gaguraczi.paw.domain.walkcourse.dto.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 코스 상세 응답")
public class WalkCourseResponse {

    @Schema(description = "코스 id", example = "1")
    private final Long courseId;

    @Schema(description = "반려동물 id", example = "1")
    private final Long petId;

    @Schema(description = "코스 이름", example = "한강공원 한 바퀴")
    private final String name;

    @Schema(description = "코스 거리(km)", example = "1.8")
    private final BigDecimal distance;

    @Schema(description = "지도 썸네일 이미지 URL", example = "https://cdn.example.com/course/1.png")
    private final String thumbnailUrl;

    @Schema(description = "경로 좌표 목록")
    private final List<Coordinate> path;

    @Schema(description = "이 코스로 산책한 횟수", example = "12")
    private final Integer useCount;

    @Schema(description = "마지막으로 걸은 시각", example = "2026-07-06T19:05:00")
    private final LocalDateTime lastUsedAt;
}
