package com.gaguraczi.paw.domain.pets.dto.req;

import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "펫 수정 요청 (multipart data part JSON, 모든 필드 선택)")
public record PetUpdateReq(
        @Schema(description = "반려동물 종류", example = "DOG")
        PetType petType,

        @Schema(description = "품종 ID (GET /breeds 검색 결과의 breedId 권장)", example = "1")
        Long breedId,

        @Size(max = 100, message = "품종명은 100자 이내이어야 합니다.")
        @Schema(description = "품종명 (breedId 없을 때 마스터 이름으로 매핑 시도)", example = "말티즈")
        String breed,

        @Size(max = 50, message = "반려동물 이름은 50자 이내이어야 합니다.")
        @Schema(description = "반려동물 이름", example = "초코")
        String petName,

        @PastOrPresent(message = "생년월일은 오늘 이전이어야 합니다.")
        @Schema(description = "생년월일", example = "2022-01-15")
        LocalDate birth,

        @DecimalMin(value = "0.01", message = "몸무게는 0보다 커야 합니다.")
        @Digits(integer = 3, fraction = 2, message = "몸무게 형식이 올바르지 않습니다.")
        @Schema(description = "몸무게(kg)", example = "3.50")
        BigDecimal petWeight,

        @Schema(description = "성별", example = "MALE")
        Gender gender,

        @Schema(description = "중성화 여부", example = "true")
        Boolean neutering
) {
}
