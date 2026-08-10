package com.gaguraczi.paw.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "카카오 로그인/연동 요청")
public class KakaoLoginReq {

    @NotBlank(message = "카카오 accessToken은 필수입니다.")
    @Schema(description = "카카오 OAuth accessToken", example = "kakao_access_token_sample")
    private String accessToken;
}
