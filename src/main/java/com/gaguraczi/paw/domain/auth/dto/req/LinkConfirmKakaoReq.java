package com.gaguraczi.paw.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "연동 확인 - 카카오 (existingProvider=KAKAO일 때)")
public class LinkConfirmKakaoReq {

    @NotBlank
    @Schema(description = "LOGIN_LINK_201 응답의 linkToken", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
    private String linkToken;

    @NotBlank
    @Schema(description = "기존 카카오 계정 확인용 accessToken", example = "kakao_access_token_sample")
    private String accessToken;
}
