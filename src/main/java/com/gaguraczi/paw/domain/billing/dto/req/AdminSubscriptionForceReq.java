package com.gaguraczi.paw.domain.billing.dto.req;

import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

@Schema(description = "관리자 요금제 강제 변경 요청")
public record AdminSubscriptionForceReq(
        @NotNull(message = "uid는 필수입니다.")
        @Schema(description = "대상 유저 uid", example = "550e8400-e29b-41d4-a716-446655440000",
                requiredMode = Schema.RequiredMode.REQUIRED)
        UUID uid,

        @NotNull(message = "요금제는 필수입니다.")
        @Schema(
                description = "강제 적용할 플랜",
                example = "BASIC",
                requiredMode = Schema.RequiredMode.REQUIRED,
                allowableValues = {"BASIC", "PRO", "ULTIMATE"}
        )
        SubscribeType plan
) {
}
