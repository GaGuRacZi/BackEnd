package com.gaguraczi.paw.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "Refresh Token 요청 (재발급/로그아웃)")
public class RefreshTokenReq {

    @NotBlank(message = "refreshToken은 필수입니다.")
    @Schema(description = "Refresh JWT", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.refresh.sample")
    private String refreshToken;
}
