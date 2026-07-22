package com.gaguraczi.paw.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkConfirmKakaoReq {

    @NotBlank
    private String linkToken;

    @NotBlank
    private String accessToken;
}
