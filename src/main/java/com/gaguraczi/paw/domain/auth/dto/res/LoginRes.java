package com.gaguraczi.paw.domain.auth.dto.res;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
public class LoginRes {

    private final String accessToken;
    private final String refreshToken;

    @Getter(AccessLevel.NONE)
    private final boolean isNew;

    private final UUID uid;

    @JsonProperty("isNew")
    public boolean getIsNew() {
        return isNew;
    }
}
