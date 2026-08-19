package com.gaguraczi.paw.domain.visit.dto.res;

import com.gaguraczi.paw.domain.visit.entity.VisitPrescription;
import com.gaguraczi.paw.domain.visit.enums.DoseFrequency;
import com.gaguraczi.paw.domain.visit.enums.MealTiming;
import com.gaguraczi.paw.domain.visit.enums.PrescriptionSource;
import com.gaguraczi.paw.domain.visit.enums.TakeTime;
import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "VisitPrescriptionRes", description = "진료에 저장된 처방 한 건. CATALOG는 medicationId가 있고, CUSTOM은 medicationId가 null입니다.")
public record VisitPrescriptionRes(
        @Schema(description = "처방 ID. 삭제 시 path의 prescriptionId로 사용합니다.", example = "10")
        Long prescriptionId,
        @Schema(description = "CATALOG 또는 CUSTOM", example = "CATALOG")
        PrescriptionSource source,
        @Schema(description = "마스터 약물 ID. CUSTOM이면 null.", example = "1", nullable = true)
        Long medicationId,
        @Schema(description = "한글 약물명", example = "카미녹스")
        String nameKo,
        @Schema(description = "영문 약물명", example = "Carprofen 25mg", nullable = true)
        String nameEn,
        @Schema(description = "성분명", example = "카르프로펜", nullable = true)
        String ingredient,
        @Schema(description = "1회 용량 숫자", example = "1", nullable = true)
        Integer dosageAmount,
        @Schema(description = "용량 단위. 미입력 시 '정'.", example = "정")
        String dosageUnit,
        @Schema(description = "복용 횟수", example = "TWICE_DAILY")
        DoseFrequency frequency,
        @Schema(description = "식사 기준 복용 시점", example = "AFTER_MEAL")
        MealTiming mealTiming,
        @Schema(description = "복용 시간대. 없으면 빈 배열.")
        List<TakeTime> takeTimes,
        @Schema(description = "주의사항", nullable = true)
        String caution
) {
    public static VisitPrescriptionRes from(VisitPrescription prescription) {
        return new VisitPrescriptionRes(
                prescription.getPrescriptionId(),
                prescription.getSource(),
                prescription.getMedication() == null ? null : prescription.getMedication().getMedicationId(),
                prescription.getNameKo(),
                prescription.getNameEn(),
                prescription.getIngredient(),
                prescription.getDosageAmount(),
                prescription.getDosageUnit(),
                prescription.getFrequency(),
                prescription.getMealTiming(),
                prescription.getTakeTimes() == null ? List.of() : List.copyOf(prescription.getTakeTimes()),
                prescription.getCaution()
        );
    }
}
