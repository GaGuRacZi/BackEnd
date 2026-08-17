package com.gaguraczi.paw.domain.walk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 기록 수정 요청 (보낸 필드만 반영)")
public class WalkUpdateRequest {

    @Schema(description = "산책 코스 id", example = "1")
    private Long courseId;

    @Schema(description = "산책 날짜", example = "2026-07-06")
    private LocalDate walkDate;

    @Schema(description = "날씨. 안 보내면 그대로 둡니다", example = "흐림",
            allowableValues = {"맑음", "흐림", "비", "눈", "바람"})
    private String weatherType;

    @Schema(description = "날씨 온도(℃)", example = "22")
    private Integer temp;

    @Schema(description = "산책 시작 시간", example = "2026-07-06T18:20:00")
    private LocalDateTime startTime;

    @Schema(description = "산책 종료 시간", example = "2026-07-06T19:05:00")
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "99.9", message = "산책 거리는 99.9 이하여야 합니다.")
    @Schema(description = "산책 거리(km)", example = "2.0")
    private BigDecimal walkingAmount;

    @Schema(description = "산책 강도. 안 보내면 그대로 둡니다", example = "활발",
            allowableValues = {"느긋", "보통", "활발"})
    private String walkType;

    @Schema(description = "소변 여부", example = "false")
    private Boolean isUrine;

    @Schema(description = "대변 여부", example = "false")
    private Boolean isStool;

    @Schema(description = "특이사항", example = "다리를 살짝 절었음")
    private String significant;
}
