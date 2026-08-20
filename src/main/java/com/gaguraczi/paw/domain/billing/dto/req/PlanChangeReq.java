package com.gaguraczi.paw.domain.billing.dto.req;

import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

@Schema(description = "요금제 변경 요청")
public record PlanChangeReq(
        @NotNull(message = "요금제는 필수입니다.")
        @Schema(
                description = "변경할 플랜",
                example = "PRO",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"BASIC", "PRO", "ULTIMATE"}
        )
        SubscribeType plan
) {
}
