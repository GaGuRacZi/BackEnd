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
    @Schema(description = "반려동물 id", example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long petId;

    @NotNull(message = "산책 날짜는 필수입니다.")
    @Schema(description = "산책 날짜(yyyy-MM-dd). 필수이지만 저장값은 startTime의 날짜를 씁니다. startTime과 같은 날을 보내세요.",
            example = "2026-07-06", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDate walkDate;

    @NotBlank(message = "날씨는 필수입니다.")
    @Schema(description = "날씨 (한글)", example = "맑음",
            allowableValues = {"맑음", "흐림", "비", "눈", "바람"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String weatherType;

    @NotNull(message = "온도는 필수입니다.")
    @Schema(description = "날씨 온도(℃)", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer temp;

    @NotNull(message = "산책 시작 시간은 필수입니다.")
    @Schema(description = "산책 시작 시간 (yyyy-MM-dd'T'HH:mm:ss). 저장 날짜의 기준이 됩니다.",
            example = "2026-07-06T18:20:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime startTime;

    @NotNull(message = "산책 종료 시간은 필수입니다.")
    @Schema(description = "산책 종료 시간. startTime보다 빠르면 안 됩니다.",
            example = "2026-07-06T19:05:00", requiredMode = Schema.RequiredMode.REQUIRED)
    private LocalDateTime endTime;

    @NotNull(message = "산책 거리는 필수입니다.")
    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "99.9", message = "산책 거리는 99.9 이하여야 합니다.")
    @Schema(description = "산책 거리(km). 0.0~99.9", example = "1.8", requiredMode = Schema.RequiredMode.REQUIRED)
    private BigDecimal walkingAmount;

    @NotBlank(message = "산책 강도는 필수입니다.")
    @Schema(description = "산책 강도 (한글)", example = "보통",
            allowableValues = {"느긋", "보통", "활발"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String walkType;

    @Schema(description = "소변 여부. 생략 시 false", example = "true")
    private Boolean isUrine;

    @Schema(description = "대변 여부. 생략 시 false", example = "true")
    private Boolean isStool;

    @Schema(description = "특이사항. 없으면 생략", example = "평소보다 힘들어 함", nullable = true)
    private String significant;
}
