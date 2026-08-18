package com.gaguraczi.paw.domain.medication.dto;

public record MedicationSearchHit(
        long medicationId,
        String nameKo,
        String nameEn,
        String ingredient,
        double score,
        boolean lexicalMatch
) {
}
