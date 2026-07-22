package com.gaguraczi.paw.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LinkConfirmLocalReq {

    @NotBlank
    private String linkToken;

    @NotBlank
    private String password;
}
