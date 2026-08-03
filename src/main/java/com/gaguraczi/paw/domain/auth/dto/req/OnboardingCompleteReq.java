package com.gaguraczi.paw.domain.auth.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

@Schema(description = "온보딩 완료 요청 (보호자/좌표/약관). 시군구는 좌표로 서버가 자동 확정합니다.")
public record OnboardingCompleteReq(
        @NotBlank(message = "이름은 필수입니다.")
        @Size(max = 10, message = "이름은 10자 이내이어야 합니다.")
        @Schema(description = "보호자 이름", example = "홍길동")
        String name,

        @NotBlank(message = "닉네임은 필수입니다.")
        @Size(max = 15, message = "닉네임은 15자 이내이어야 합니다.")
        @Pattern(regexp = "^[a-zA-Z0-9가-힣]+$", message = "닉네임은 15자 이내의 영문, 숫자, 한글만 사용 가능합니다.")
        @Schema(description = "닉네임", example = "길동이")
        String nickname,

        @Size(max = 30, message = "한줄소개는 30자 이내이어야 합니다.")
        @Schema(description = "한줄소개", example = "강아지와 산책하는 걸 좋아해요")
        String intro,

        @NotNull(message = "위치 정보는 필수입니다.")
        @Valid
        @Schema(description = "위치 정보 (위·경도만)")
        LocationInfo location,

        @NotNull(message = "약관 동의 정보는 필수입니다.")
        @Valid
        @Schema(description = "약관 동의 정보")
        TermsAgreementsReq agreements
) {
    @Schema(description = "위치 좌표")
    public record LocationInfo(
            @NotNull(message = "위도는 필수입니다.")
            @DecimalMin(value = "-90.0", message = "위도 범위가 올바르지 않습니다.")
            @DecimalMax(value = "90.0", message = "위도 범위가 올바르지 않습니다.")
            @Schema(description = "위도", example = "37.5665")
            Double latitude,

            @NotNull(message = "경도는 필수입니다.")
            @DecimalMin(value = "-180.0", message = "경도 범위가 올바르지 않습니다.")
            @DecimalMax(value = "180.0", message = "경도 범위가 올바르지 않습니다.")
            @Schema(description = "경도", example = "126.9780")
            Double longitude
    ) {
    }
}
