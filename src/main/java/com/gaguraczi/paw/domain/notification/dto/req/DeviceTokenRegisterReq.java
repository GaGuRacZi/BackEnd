package com.gaguraczi.paw.domain.notification.dto.req;

import com.gaguraczi.paw.domain.notification.enums.DevicePlatform;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "디바이스 토큰 등록/갱신 요청")
public record DeviceTokenRegisterReq(
        @Schema(example = "f1Qk...fcmtoken") @NotBlank String token,
        @Schema(example = "ANDROID") @NotNull DevicePlatform platform
) {
}
