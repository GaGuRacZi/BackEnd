package com.gaguraczi.paw.domain.walk.dto.response;

import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.enums.WalkTypeEnum;
import com.gaguraczi.paw.domain.walk.enums.WeatherTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 기록 상세 응답")
public class WalkResponse {

    @Schema(description = "산책 id. 진행 중(IN_PROGRESS)이면 null. 완료 후 상세/수정/삭제에 사용",
            example = "1", nullable = true)
    private final Long walkId;

    @Schema(description = "반려동물 id", example = "1")
    private final Long petId;

    @Schema(description = "산책 날짜 (startTime 기준)", example = "2026-07-06")
    private final LocalDate walkDate;

    @Schema(description = "날씨 한글. 진행 중이면 null", example = "맑음", nullable = true)
    private final WeatherTypeEnum weatherType;

    @Schema(description = "날씨 온도(℃). 진행 중이면 null", example = "24", nullable = true)
    private final Integer temp;

    @Schema(description = "산책 시작 시간. 타이머 복구 시 경과 시간 계산 기준", example = "2026-07-06T18:20:00")
    private final LocalDateTime startTime;

    @Schema(description = "산책 종료 시간. 진행 중이면 null", example = "2026-07-06T19:05:00", nullable = true)
    private final LocalDateTime endTime;

    @Schema(description = "총 소요 시간(분). 서버가 start~end로 계산. 진행 중이면 null",
            example = "45", nullable = true)
    private final Long durationMinutes;

    @Schema(description = "산책 거리(km). 진행 중이면 0", example = "1.8")
    private final BigDecimal walkingAmount;

    @Schema(description = "산책 강도 한글. 진행 중이면 플레이스홀더 '보통'", example = "보통")
    private final WalkTypeEnum walkType;

    @Schema(description = "소변 여부. 진행 중이면 false(플레이스홀더)", example = "true")
    private final Boolean isUrine;

    @Schema(description = "대변 여부. 진행 중이면 false(플레이스홀더)", example = "true")
    private final Boolean isStool;

    @Schema(description = "특이사항. 진행 중이거나 없으면 null", example = "평소보다 힘들어 함", nullable = true)
    private final String significant;

    @Schema(description = "IN_PROGRESS=타이머 중(미저장), COMPLETED=DB 저장 완료", example = "COMPLETED")
    private final WalkStatusEnum walkStatus;
}
