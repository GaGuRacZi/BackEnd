package com.gaguraczi.paw.domain.users.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "FCM 푸시 토큰 등록/해제. null·빈 문자열이면 해제(로그아웃 시에도 호출 권장)")
public record PushTokenUpdateReq(
        @Schema(description = "FCM 등록 토큰. 비어 있으면 서버에서 토큰을 지웁니다.", example = "fcm-device-token")
        String pushToken
) {
}
