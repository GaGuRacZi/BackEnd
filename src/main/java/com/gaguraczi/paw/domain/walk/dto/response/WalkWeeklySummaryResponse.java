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

    @Schema(description = "이번 주 1회당 평균 산책 시간(분)", example = "45")
    private final Long averageMinutes;

    @Schema(description = "지난주 1회당 평균 산책 시간(분)", example = "35")
    private final Long lastWeekAverageMinutes;

    @Schema(description = "지난주 대비 증감(분). 양수면 늘어난 것", example = "10")
    private final Long diffMinutes;

    @Schema(description = "이번 주 산책 횟수", example = "5")
    private final Integer walkCount;
    //이건 안 해도 되려나... 해야되지 않을까 평균 시간을 알려면

    @Schema(description = "이번 주 총 산책 시간(분)", example = "225")
    private final Long totalMinutes;

    @Schema(description = "이번 주 총 산책 거리(km)", example = "9.4")
    private final BigDecimal totalDistance;
}
