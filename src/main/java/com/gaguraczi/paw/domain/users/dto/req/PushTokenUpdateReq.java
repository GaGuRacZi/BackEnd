package com.gaguraczi.paw.domain.users.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 푸시 토큰 등록/해제. 빈 값이면 해제")
public record PushTokenUpdateReq(
        @Schema(description = "FCM 등록 토큰. 빈 문자열이면 해제", example = "fcm-device-token")
        String pushToken
) {
}
