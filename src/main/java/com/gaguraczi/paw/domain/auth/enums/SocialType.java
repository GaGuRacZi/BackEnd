package com.gaguraczi.paw.domain.auth.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "로그인/연동 수단")
public enum SocialType {
    @Schema(description = "카카오")
    KAKAO,
    @Schema(description = "이메일(로컬)")
    LOCAL
}
