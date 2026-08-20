package com.gaguraczi.paw.domain.walk.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
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
    @Schema(description = "반려동물 id. 종료(finish)·진행 중 조회에도 그대로 사용",
            example = "1", requiredMode = Schema.RequiredMode.REQUIRED)
    private Long petId;

    @Schema(description = "산책 시작 시간 (yyyy-MM-dd'T'HH:mm:ss). 생략 시 서버 현재 시각",
            example = "2026-07-06T18:20:00")
    private LocalDateTime startTime;
}
