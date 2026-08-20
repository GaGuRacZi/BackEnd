package com.gaguraczi.paw.domain.visit.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class VisitPetDisplayTest {

    @Test
    void KST_날짜_경계에서_한_살이_된다() {
        LocalDate birth = LocalDate.of(2025, 8, 21);

        assertThat(VisitPetDisplay.ageLabel(birth, LocalDate.of(2026, 8, 21)))
                .isEqualTo("1살 0개월");
        assertThat(VisitPetDisplay.ageLabel(birth, LocalDate.of(2026, 8, 20)))
                .isEqualTo("11개월");
    }

    @Test
    void 생년월일이_없으면_null이다() {
        assertThat(VisitPetDisplay.ageLabel(null, LocalDate.of(2026, 8, 21))).isNull();
    }
}
