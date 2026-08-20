package com.gaguraczi.paw.domain.walkcourse.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

//위경도 좌표
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Schema(description = "위경도 좌표")
public class Coordinate {

    @NotNull(message = "위도는 필수입니다.")
    @Schema(description = "위도", example = "37.5665")
    @DecimalMin(value = "-90.0")
    @DecimalMax(value = "90.0")
    private Double lat;


    @NotNull(message = "경도는 필수입니다.")
    @Schema(description = "경도", example = "126.9780")
    @DecimalMin(value = "-180.0")
    @DecimalMax(value = "180.0")
    private Double lng;
}
