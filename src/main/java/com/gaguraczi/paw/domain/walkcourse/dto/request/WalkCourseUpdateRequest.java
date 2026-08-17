package com.gaguraczi.paw.domain.walkcourse.dto.request;

import com.gaguraczi.paw.domain.walkcourse.dto.Coordinate;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
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
@Schema(description = "산책 코스 수정 요청 (보낸 필드만 반영)")
public class WalkCourseUpdateRequest {

    @Size(max = 50, message = "코스 이름은 50자 이하여야 합니다.")
    @Schema(description = "코스 이름", example = "한강공원 두 바퀴")
    private String name;

    @DecimalMin(value = "0.0", message = "코스 거리는 0 이상이어야 합니다.")
    @DecimalMax(value = "999.9", message = "코스 거리는 999.9 이하여야 합니다.")
    @Schema(description = "코스 거리(km)", example = "3.6")
    private BigDecimal distance;

    @Schema(description = "지도 썸네일 이미지 URL", example = "https://cdn.example.com/course/1.png")
    private String thumbnailUrl;
    //api 연동해서 쓰면 이미지 URL은 필요가 없으려나

    @Valid
    @Schema(description = "경로 좌표 목록")
    private List<Coordinate> path;
}
