package com.gaguraczi.paw.domain.todo.support;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class TodoRemindAtTest {

    @Test
    void date와_todoTime을_KST_instant로_합친다() {
        var instant = TodoRemindAt.of(LocalDate.of(2026, 8, 19), LocalTime.of(20, 0));
        assertThat(instant).isEqualTo(
                java.time.ZonedDateTime.of(2026, 8, 19, 20, 0, 0, 0, TodoRemindAt.KST).toInstant()
        );
    }

    @Test
    void todoTime이_없으면_null이다() {
        assertThat(TodoRemindAt.of(LocalDate.of(2026, 8, 19), null)).isNull();
    }
}
