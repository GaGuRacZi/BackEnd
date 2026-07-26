package com.gaguraczi.paw.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class KakaoLoginReq {

    @NotBlank(message = "카카오 accessToken은 필수입니다.")
    private String accessToken;
}
