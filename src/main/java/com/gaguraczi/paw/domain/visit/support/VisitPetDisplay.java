package com.gaguraczi.paw.domain.visit.support;

import com.gaguraczi.paw.domain.users.entity.Pet;

import java.time.LocalDate;
import java.time.Period;

public final class VisitPetDisplay {

    private VisitPetDisplay() {
    }

    public static String breedName(Pet pet) {
        if (pet.getBreed() != null && pet.getBreed().getName() != null && !pet.getBreed().getName().isBlank()) {
            return pet.getBreed().getName();
        }
        return pet.getBreedName();
    }

    public static String ageLabel(LocalDate birth) {
        return ageLabel(birth, LocalDate.now());
    }

    static String ageLabel(LocalDate birth, LocalDate today) {
        if (birth == null) {
            return null;
        }
        Period period = Period.between(birth, today);
        int years = Math.max(period.getYears(), 0);
        int months = Math.max(period.getMonths(), 0);
        if (years <= 0) {
            return months + "개월";
        }
        return years + "살 " + months + "개월";
    }
}
