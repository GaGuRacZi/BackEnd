package com.gaguraczi.paw.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

//일 별 그래프
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "일별 산책 통계 응답")
public class WalkDailyStatResponse {

    @Schema(description = "날짜", example = "2026-07-06")
    private final LocalDate walkDate;

    @Schema(description = "요일 한글 한 글자. 그래프 X축 라벨 (`월`~`일`)", example = "월")
    private final String dayOfWeek;

    @Schema(description = "그 날 총 산책 시간(분). 안 했으면 0", example = "45")
    private final Long totalMinutes;

    @Schema(description = "그 날 총 산책 거리(km). 안 했으면 0", example = "1.8")
    private final BigDecimal totalDistance;

    @Schema(description = "그 날 산책 횟수. 안 했으면 0", example = "1")
    private final Integer walkCount;
}
