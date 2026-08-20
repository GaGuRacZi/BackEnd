package com.gaguraczi.paw.domain.walk.dto.response;


import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.enums.WalkTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

//산책 기록 목록용 응답

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 기록 목록 한 줄. 완료 기록만 포함됩니다.")
public class WalkSummaryResponse {

    @Schema(description = "산책 id", example = "1")
    private final Long walkId;

    @Schema(description = "산책 날짜", example = "2026-07-06")
    private final LocalDate walkDate;

    @Schema(description = "산책 시작 시간", example = "2026-07-06T18:20:00")
    private final LocalDateTime startTime;

    @Schema(description = "산책 종료 시간", example = "2026-07-06T19:05:00")
    private final LocalDateTime endTime;

    @Schema(description = "총 소요 시간(분)", example = "45")
    private final Long durationMinutes;

    @Schema(description = "산책 거리(km)", example = "1.8")
    private final BigDecimal walkingAmount;

    @Schema(description = "산책 강도 한글. 목록 카드에 그대로 표시", example = "보통")
    private final WalkTypeEnum walkType;

    @Schema(description = "COMPLETED 고정. 진행 중 타이머는 목록에 없습니다", example = "COMPLETED")
    private final WalkStatusEnum walkStatus;
}
