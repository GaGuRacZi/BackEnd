package com.gaguraczi.paw.domain.rag.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PgVectorLiteralTest {

    @Test
    void formatsFloatArray() {
        assertThat(PgVectorLiteral.of(new float[]{0.1f, -0.2f, 0.3f}))
                .isEqualTo("[0.1,-0.2,0.3]");
    }

    @Test
    void rejectsEmpty() {
        assertThatThrownBy(() -> PgVectorLiteral.of(new float[0]))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
