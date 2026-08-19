package com.gaguraczi.paw.domain.medication.dto.res;

import com.gaguraczi.paw.domain.medication.entity.Medication;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "약물 마스터 상세. 진료 CATALOG 처방에서 caution을 생략하면 precautionMd 첫 줄이 들어갑니다.")
public class MedicationDetailRes {

    @Schema(description = "약물 ID", example = "1")
    private final Long medicationId;

    @Schema(description = "한글 제품명", example = "카미녹스")
    private final String nameKo;

    @Schema(description = "영문 제품명", example = "Carprofen 25mg")
    private final String nameEn;

    @Schema(description = "성분명", example = "카르프로펜")
    private final String ingredient;

    @Schema(description = "약 설명 마크다운")
    private final String descriptionMd;

    @Schema(description = "주의할 점 마크다운")
    private final String precautionMd;

    public static MedicationDetailRes from(Medication medication) {
        return MedicationDetailRes.builder()
                .medicationId(medication.getMedicationId())
                .nameKo(medication.getNameKo())
                .nameEn(medication.getNameEn())
                .ingredient(medication.getIngredient())
                .descriptionMd(medication.getDescriptionMd())
                .precautionMd(medication.getPrecautionMd())
                .build();
    }
}
