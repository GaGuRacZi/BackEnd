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
import java.time.LocalDateTime;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 자동기록 종료 요청")
public class WalkFinishRequest {

    @NotNull(message = "반려동물 id는 필수입니다.")
    @Schema(description = "반려동물 id. walkId가 아니라 시작 때 쓴 petId를 보냅니다.",
            example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long petId;

    @Schema(description = "산책 종료 시간. 생략 시 서버 현재 시각. startTime보다 빠르면 안 됩니다.",
            example = "2026-07-06T19:05:00")
    private LocalDateTime endTime;

    @NotBlank(message = "날씨는 필수입니다.")
    @Schema(description = "날씨 (한글)", example = "맑음",
            allowableValues = {"맑음", "흐림", "비", "눈", "바람"},
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String weatherType;

    @NotNull(message = "온도는 필수입니다.")
    @Schema(description = "날씨 온도(℃)", example = "24", requiredMode = Schema.RequiredMode.REQUIRED)
    private Integer temp;

    @NotNull(message = "산책 거리는 필수입니다.")
    @DecimalMin(value = "0.0", message = "산책 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "99.9", message = "산책 거리는 99.9 이하여야 합니다.")
    @Schema(description = "산책 거리(km). 앱에서 측정한 값, 0.0~99.9",
            example = "1.8", requiredMode = Schema.RequiredMode.REQUIRED)
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

    @Schema(description = "특이사항", example = "평소보다 힘들어 함", nullable = true)
    private String significant;
}
