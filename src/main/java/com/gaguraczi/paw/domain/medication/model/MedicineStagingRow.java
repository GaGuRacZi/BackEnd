package com.gaguraczi.paw.domain.medication.model;

public record MedicineStagingRow(
        String itemSeq,
        String productName,
        String productNameEn,
        String ingredients,
        String efficacy,
        String dosage,
        String precaution,
        String targetAnimal
) {
}
