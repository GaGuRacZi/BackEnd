package com.gaguraczi.paw.domain.weights.dto.req;

import com.gaguraczi.paw.domain.weights.enums.AppetiteTypeEnum;
import com.gaguraczi.paw.domain.weights.enums.BodyTypeEnum;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Schema(name = "PetWeightCreateReq", description = "체중 기록 저장 요청")
public record PetWeightCreateReq(

        @Schema(description = "몸무게(kg). 0.01 이상, 소수 둘째 자리까지", example = "4.20",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @DecimalMin(value = "0.01", message = "몸무게는 0보다 커야 합니다.")
        @Digits(integer = 3, fraction = 2)
        BigDecimal weight,

        @Schema(description = "체형. SKINNY=마름, HEALTHY=적정, OVER_WEIGHT=과체중", example = "HEALTHY",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        BodyTypeEnum bodyType,

        @Schema(description = "식욕. LOW=식욕이 떨어짐, MIDDLE=식욕 평범, HIGH=식욕이 많음", example = "LOW",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        AppetiteTypeEnum appetiteType,

        @Schema(description = "메모 (최대 1000자)", example = "식사 후 같은 시간대에 측정했어요.")
        @Size(max = 1000)
        String memoContent,

        @Schema(description = "측정 시각 (yyyy-MM-dd'T'HH:mm:ss). 미래 불가", example = "2026-07-06T20:30:00",
                requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        LocalDateTime recordedAt
) {
}
