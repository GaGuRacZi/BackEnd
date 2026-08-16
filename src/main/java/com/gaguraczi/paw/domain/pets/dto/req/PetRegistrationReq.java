package com.gaguraczi.paw.domain.pets.dto.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "동물등록증 등록/수정 요청 (multipart data part JSON)")
public record PetRegistrationReq(
        @NotBlank(message = "보호자 이름은 필수입니다.")
        @Size(max = 50, message = "보호자 이름은 50자 이내이어야 합니다.")
        @Schema(description = "보호자 이름", example = "김지현", requiredMode = Schema.RequiredMode.REQUIRED)
        String guardianName,

        @NotBlank(message = "동물등록번호는 필수입니다.")
        @Size(max = 50, message = "동물등록번호는 50자 이내이어야 합니다.")
        @Schema(description = "동물등록번호", example = "410000012345678", requiredMode = Schema.RequiredMode.REQUIRED)
        String registrationNumber
) {
}
