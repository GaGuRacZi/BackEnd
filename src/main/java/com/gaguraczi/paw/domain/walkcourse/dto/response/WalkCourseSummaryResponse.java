package com.gaguraczi.paw.domain.walkcourse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 코스 요약 응답")
public class WalkCourseSummaryResponse {

    @Schema(description = "코스 id", example = "1")
    private final Long courseId;

    @Schema(description = "코스 이름", example = "한강공원 한 바퀴")
    private final String name;

    @Schema(description = "코스 거리(km)", example = "1.8")
    private final BigDecimal distance;

    @Schema(description = "지도 썸네일 이미지 URL", example = "https://cdn.example.com/course/1.png")
    private final String thumbnailUrl;
    //얘도.. URL 안 해도 되려나...

    @Schema(description = "이 코스로 산책한 횟수", example = "12")
    private final Integer useCount;
}
