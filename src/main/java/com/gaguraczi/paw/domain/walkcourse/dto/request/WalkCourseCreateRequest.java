package com.gaguraczi.paw.domain.walkcourse.dto.request;

import com.gaguraczi.paw.domain.walkcourse.dto.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "산책 코스 등록 요청")
public class WalkCourseCreateRequest {

    @NotNull(message = "반려동물 id는 필수입니다.")
    @Schema(description = "반려동물 id", example = "1")
    private Long petId;

    @NotBlank(message = "코스 이름은 필수입니다.")
    @Size(max = 50, message = "코스 이름은 50자 이하여야 합니다.")
    @Schema(description = "코스 이름", example = "한강공원 한 바퀴")
    private String name;

    @NotNull(message = "코스 거리는 필수입니다.")
    @DecimalMin(value = "0.0", message = "코스 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "999.9", message = "코스 거리는 999.9 이하여야 합니다.")
    @Schema(description = "코스 거리(km). 네이버 지도 API로 측정한 값", example = "1.8")
    private BigDecimal distance;

    @Schema(description = "지도 썸네일 이미지 URL", example = "https://cdn.example.com/course/1.png")
    private String thumbnailUrl;

    @Valid
    @Schema(description = "경로 좌표 목록 (순서대로 이으면 코스 선이 됨)")
    private List<Coordinate> path;
}
