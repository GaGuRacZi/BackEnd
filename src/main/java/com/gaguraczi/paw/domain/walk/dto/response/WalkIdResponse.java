package com.gaguraczi.paw.domain.walk.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 id 응답")
public class WalkIdResponse {

    @Schema(description = "산책 id", example = "1")
    private final Long walkId;
}
