package com.gaguraczi.paw.domain.visit.dto.req;

import com.gaguraczi.paw.domain.visit.enums.DoseFrequency;
import com.gaguraczi.paw.domain.visit.enums.MealTiming;
import com.gaguraczi.paw.domain.visit.enums.PrescriptionSource;
import com.gaguraczi.paw.domain.visit.enums.TakeTime;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Schema(
        name = "VisitPrescriptionAddReq",
        description = """
                진료에 처방 약물을 추가합니다. status=READY일 때만 가능합니다.
                source·frequency·mealTiming은 항상 필수입니다.
                CATALOG이면 medicationId 필수(마스터 없는 ID는 MEDICATION_404).
                CUSTOM이면 nameKo 필수.
                OCR 처방전 업로드는 지원하지 않습니다.
                """
)
public record VisitPrescriptionAddReq(
        @NotNull
        @Schema(
                description = "CATALOG=마스터 검색 선택, CUSTOM=기타 직접 입력",
                example = "CATALOG",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        PrescriptionSource source,
        @Schema(description = "마스터 약물 ID. source=CATALOG일 때 필수. GET /medications 결과의 medicationId.", example = "1")
        Long medicationId,
        @Schema(description = "한글 약물명. source=CUSTOM일 때 필수. CATALOG이면 무시되고 마스터 nameKo가 들어갑니다.", example = "관절영양제")
        String nameKo,
        @Schema(description = "영문명. CUSTOM에서만 사용. CATALOG이면 마스터 nameEn.", example = "Glucosamine")
        String nameEn,
        @Schema(description = "성분명. CUSTOM에서만 사용. CATALOG이면 마스터 ingredient.", example = "글루코사민")
        String ingredient,
        @Schema(description = "1회 용량 숫자. 생략 가능.", example = "1")
        Integer dosageAmount,
        @Schema(description = "용량 단위. 생략하거나 빈 문자열이면 기본값 '정'.", example = "정")
        String dosageUnit,
        @NotNull
        @Schema(description = "복용 횟수", example = "TWICE_DAILY", requiredMode = Schema.RequiredMode.REQUIRED)
        DoseFrequency frequency,
        @NotNull
        @Schema(description = "식사 기준 복용 시점", example = "AFTER_MEAL", requiredMode = Schema.RequiredMode.REQUIRED)
        MealTiming mealTiming,
        @Schema(description = "복용 시간대 배열. 생략 또는 null이면 빈 배열로 저장됩니다.", example = "[\"MORNING\", \"EVENING\"]")
        List<TakeTime> takeTimes,
        @Schema(
                description = "주의사항. CATALOG에서 생략하면 마스터 precautionMd의 첫 비어 있지 않은 줄이 들어갑니다. CUSTOM은 생략 시 null.",
                example = "위장 장애가 있으면 수의사와 상담하세요."
        )
        String caution
) {
}
