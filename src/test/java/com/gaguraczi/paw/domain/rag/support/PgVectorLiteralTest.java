package com.gaguraczi.paw.domain.rag.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgVectorLiteralTest {

    @Test
    void formatsFiniteValues() {
        assertThat(PgVectorLiteral.of(new float[]{0.1f, -2f})).isEqualTo("[0.1,-2.0]");
    }

    @Test
    void rejectsNullOrEmpty() {
        assertThatThrownBy(() -> PgVectorLiteral.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PgVectorLiteral.of(new float[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejectsNonFiniteValues() {
        assertThatThrownBy(() -> PgVectorLiteral.of(new float[]{Float.NaN}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PgVectorLiteral.of(new float[]{Float.POSITIVE_INFINITY}))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> PgVectorLiteral.of(new float[]{Float.NEGATIVE_INFINITY}))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
