package com.gaguraczi.paw.domain.visit.support;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VisitTextLimitsTest {

    @Test
    void acceptsInclusiveBounds() {
        assertThat(VisitTextLimits.inRange("a".repeat(1000), 1000, 1500)).isTrue();
        assertThat(VisitTextLimits.inRange("a".repeat(1500), 1000, 1500)).isTrue();
        assertThat(VisitTextLimits.inRange("a".repeat(1250), 1000, 1500)).isTrue();
    }

    @Test
    void rejectsOutsideBoundsAndNull() {
        assertThat(VisitTextLimits.inRange(null, 1000, 1500)).isFalse();
        assertThat(VisitTextLimits.inRange("a".repeat(999), 1000, 1500)).isFalse();
        assertThat(VisitTextLimits.inRange("a".repeat(1501), 1000, 1500)).isFalse();
        assertThat(VisitTextLimits.inRange("", 1000, 1500)).isFalse();
    }
}
