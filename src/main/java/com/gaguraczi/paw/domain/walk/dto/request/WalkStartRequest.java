package com.gaguraczi.paw.domain.walk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 자동기록 시작 요청")
public class WalkStartRequest {

    @NotNull(message = "반려동물 id는 필수입니다.")
    @Schema(description = "반려동물 id", example = "1")
    private Long petId;

    @Schema(description = "산책 코스 id (코스를 골랐을 때만). 안 고르면 null", example = "1")
    private Long courseId;

    @NotBlank(message = "날씨는 필수입니다.")
    @Schema(description = "날씨", example = "맑음",
            allowableValues = {"맑음", "흐림", "비", "눈", "바람"})
    private String weatherType;

    @NotNull(message = "온도는 필수입니다.")
    @Schema(description = "날씨 온도(℃)", example = "24")
    private Integer temp;

    @Schema(description = "산책 시작 시간. 안 보내면 서버 현재 시각으로 자동 설정",
            example = "2026-07-06T18:20:00")
    private LocalDateTime startTime;
}
