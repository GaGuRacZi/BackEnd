package com.gaguraczi.paw.domain.walk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@Schema(description = "산책 수동 기록 저장 요청")
public class WalkCreateRequest {

    @NotNull(message = "반려동물 id는 필수입니다.")
    @Schema(description = "반려동물 id", example = "1")
    private Long petId;

    @Schema(description = "산책 코스 id (코스를 골랐을 때만). 안 고르면 null", example = "1")
    private Long courseId;

    @NotNull(message = "산책 날짜는 필수입니다.")
    @Schema(description = "산책 날짜", example = "2026-07-06")
    private LocalDate walkDate;

    @NotBlank(message = "날씨는 필수입니다.")
    @Schema(description = "날씨", example = "맑음",
            allowableValues = {"맑음", "흐림", "비", "눈", "바람"})
    private String weatherType;

    @NotNull(message = "온도는 필수입니다.")
    @Schema(description = "날씨 온도(℃)", example = "24")
    private Integer temp;

    @NotNull(message = "산책 시작 시간은 필수입니다.")
    @Schema(description = "산책 시작 시간", example = "2026-07-06T18:20:00")
    private LocalDateTime startTime;

    @NotNull(message = "산책 종료 시간은 필수입니다.")
    @Schema(description = "산책 종료 시간", example = "2026-07-06T19:05:00")
    private LocalDateTime endTime;

    @NotNull(message = "산책 거리는 필수입니다.")
    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "99.9", message = "산책 거리는 99.9 이하여야 합니다.")
    @Schema(description = "산책 거리(km)", example = "1.8")
    private BigDecimal walkingAmount;

    @NotBlank(message = "산책 강도는 필수입니다.")
    @Schema(description = "산책 강도", example = "보통",
            allowableValues = {"느긋", "보통", "활발"})
    private String walkType;

    @Schema(description = "소변 여부", example = "true")
    private Boolean isUrine;

    @Schema(description = "대변 여부", example = "true")
    private Boolean isStool;

    @Schema(description = "특이사항", example = "평소보다 힘들어 함")
    private String significant;
}
