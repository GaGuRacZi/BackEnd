package com.gaguraczi.paw.domain.medication.support;

import com.gaguraczi.paw.domain.medication.model.MedicineStagingRow;

public final class MedicationSearchText {

    private MedicationSearchText() {
    }

    public static String of(MedicineStagingRow row, String descriptionMd, String precautionMd) {
        StringBuilder sb = new StringBuilder();
        append(sb, "이름", row.productName());
        append(sb, "영문명", row.productNameEn());
        append(sb, "성분", row.ingredients());
        append(sb, "대상", row.targetAnimal());
        append(sb, "효능", row.efficacy());
        append(sb, "용법", row.dosage());
        append(sb, "주의(원본)", row.precaution());
        appendBlock(sb, "약 설명", descriptionMd);
        appendBlock(sb, "주의할 점", precautionMd);
        return sb.toString().trim();
    }

    private static void append(StringBuilder sb, String label, String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append('\n');
        }
        sb.append(label).append(": ").append(trimmed);
    }

    private static void appendBlock(StringBuilder sb, String label, String value) {
        String trimmed = blankToNull(value);
        if (trimmed == null) {
            return;
        }
        if (!sb.isEmpty()) {
            sb.append("\n\n");
        }
        sb.append(label).append(":\n").append(trimmed);
    }

    private static String blankToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
