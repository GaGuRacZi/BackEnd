package com.gaguraczi.paw.domain.medication.dto.res;

import com.gaguraczi.paw.domain.medication.dto.MedicationSearchHit;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "약물 검색 결과")
public class MedicationSearchRes {

    @Schema(description = "약물 ID", example = "1")
    private final Long medicationId;

    @Schema(description = "한글 제품명", example = "카미녹스")
    private final String nameKo;

    @Schema(description = "영문 제품명", example = "Carprofen 25mg")
    private final String nameEn;

    @Schema(description = "성분명", example = "카르프로펜")
    private final String ingredient;

    public static MedicationSearchRes from(MedicationSearchHit hit) {
        return MedicationSearchRes.builder()
                .medicationId(hit.medicationId())
                .nameKo(hit.nameKo())
                .nameEn(hit.nameEn())
                .ingredient(hit.ingredient())
                .build();
    }
}
