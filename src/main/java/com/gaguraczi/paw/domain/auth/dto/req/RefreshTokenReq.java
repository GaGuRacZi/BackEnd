package com.gaguraczi.paw.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshTokenReq {

    @NotBlank(message = "refreshToken은 필수입니다.")
    private String refreshToken;
}
