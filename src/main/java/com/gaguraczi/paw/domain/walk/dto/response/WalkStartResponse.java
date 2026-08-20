package com.gaguraczi.paw.domain.walk.dto.response;

import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.enums.WeatherTypeEnum;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDate;
import java.time.LocalDateTime;

//타이머 시작
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 자동기록 시작 응답")
public class WalkStartResponse {

    @Schema(description = "생성된 산책 id. 종료 API에 그대로 사용", example = "1")
    private final Long walkId;

    @Schema(description = "반려동물 id", example = "1")
    private final Long petId;

    @Schema(description = "산책 날짜", example = "2026-07-06")
    private final LocalDate walkDate;

    @Schema(description = "산책 시작 시간 (타이머 기준 시각)", example = "2026-07-06T18:20:00")
    private final LocalDateTime startTime;

    @Schema(description = "날씨", example = "맑음")
    private final WeatherTypeEnum weatherType;

    @Schema(description = "날씨 온도(℃)", example = "24")
    private final Integer temp;

    @Schema(description = "산책 진행 상태", example = "IN_PROGRESS")
    private final WalkStatusEnum walkStatus;
}
