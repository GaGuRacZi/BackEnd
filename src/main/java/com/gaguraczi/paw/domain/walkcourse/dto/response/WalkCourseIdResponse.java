package com.gaguraczi.paw.domain.walkcourse.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;


@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 코스 id 응답")
public class WalkCourseIdResponse {

    @Schema(description = "코스 id", example = "1")
    private final Long courseId;
}
