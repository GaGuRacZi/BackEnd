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

    @Test
    void truncatesToMaxInclusive() {
        assertThat(VisitTextLimits.truncate("abcd", 3)).isEqualTo("abc");
        assertThat(VisitTextLimits.truncate("abc", 3)).isEqualTo("abc");
        assertThat(VisitTextLimits.truncate(null, 3)).isEmpty();
    }
}
