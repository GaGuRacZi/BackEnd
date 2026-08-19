package com.gaguraczi.paw.domain.notification.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "디바이스 토큰 삭제(로그아웃) 요청")
public record DeviceTokenDeleteReq(
        @Schema(example = "f1Qk...fcmtoken") @NotBlank String token
) {
}
