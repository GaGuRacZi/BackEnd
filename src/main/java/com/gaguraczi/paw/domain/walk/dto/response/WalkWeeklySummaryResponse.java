package com.gaguraczi.paw.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

//주간 산책 요약
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "주간 산책 요약 응답")
public class WalkWeeklySummaryResponse {

    @Schema(description = "이번 주 시작일(월요일)", example = "2026-07-06")
    private final LocalDate weekStartDate;

    @Schema(description = "이번 주 종료일(일요일)", example = "2026-07-12")
    private final LocalDate weekEndDate;

    @Schema(description = "이번 주 1회당 평균 산책 시간(분). 총분/횟수 정수 나눗셈. 없으면 0", example = "45")
    private final Long averageMinutes;

    @Schema(description = "지난주 1회당 평균 산책 시간(분). 없으면 0", example = "35")
    private final Long lastWeekAverageMinutes;

    @Schema(description = "지난주 대비 평균 증감(분). 양수=늘어남, 음수=줄어듦", example = "10")
    private final Long diffMinutes;

    @Schema(description = "이번 주 산책 횟수", example = "5")
    private final Integer walkCount;

    @Schema(description = "이번 주 총 산책 시간(분)", example = "225")
    private final Long totalMinutes;

    @Schema(description = "이번 주 총 산책 거리(km)", example = "9.4")
    private final BigDecimal totalDistance;
}
