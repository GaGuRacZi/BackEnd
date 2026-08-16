package com.gaguraczi.paw.domain.pets.dto.req;

import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "펫 등록 요청 (multipart data part JSON). breedId 또는 breed 중 하나 필수.")
public record PetCreateReq(
        @NotNull(message = "반려동물 종류는 필수입니다.")
        @Schema(description = "반려동물 종류 (DOG/CAT 등)", example = "DOG", requiredMode = Schema.RequiredMode.REQUIRED)
        PetType petType,

        @Schema(description = "품종 ID (GET /breeds 검색 결과의 breedId 권장)", example = "1")
        Long breedId,

        @Size(max = 100, message = "품종명은 100자 이내이어야 합니다.")
        @Schema(description = "품종명 (breedId 없을 때 마스터 이름으로 매핑 시도)", example = "말티즈")
        String breed,

        @NotBlank(message = "반려동물 이름은 필수입니다.")
        @Size(max = 50, message = "반려동물 이름은 50자 이내이어야 합니다.")
        @Schema(description = "반려동물 이름 (최대 50자)", example = "초코", requiredMode = Schema.RequiredMode.REQUIRED)
        String petName,

        @NotNull(message = "생년월일은 필수입니다.")
        @PastOrPresent(message = "생년월일은 오늘 이전이어야 합니다.")
        @Schema(description = "생년월일 (오늘 이전)", example = "2022-01-15", requiredMode = Schema.RequiredMode.REQUIRED)
        LocalDate birth,

        @NotNull(message = "몸무게는 필수입니다.")
        @DecimalMin(value = "0.01", message = "몸무게는 0보다 커야 합니다.")
        @Digits(integer = 3, fraction = 2, message = "몸무게 형식이 올바르지 않습니다.")
        @Schema(description = "몸무게(kg)", example = "3.50", requiredMode = Schema.RequiredMode.REQUIRED)
        BigDecimal petWeight,

        @NotNull(message = "성별은 필수입니다.")
        @Schema(description = "성별 (MALE/FEMALE 등)", example = "MALE", requiredMode = Schema.RequiredMode.REQUIRED)
        Gender gender,

        @NotNull(message = "중성화 여부는 필수입니다.")
        @Schema(description = "중성화 여부", example = "true", requiredMode = Schema.RequiredMode.REQUIRED)
        Boolean neutering,

        @Schema(
                description = "혈액형. DOG: NONE/DEA_1_1_POSITIVE/DEA_1_1_NEGATIVE/UNKNOWN, CAT: NONE/A/B/AB/UNKNOWN. 미입력 시 NONE",
                example = "DEA_1_1_POSITIVE"
        )
        String bloodType
) {
}
