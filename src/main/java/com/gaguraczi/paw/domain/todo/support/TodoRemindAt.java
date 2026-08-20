package com.gaguraczi.paw.domain.todo.support;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/** `todo_date.date` + `todo.todo_time` 을 KST timestamptz(`remind_at`)로 합친다. */
public final class TodoRemindAt {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    private TodoRemindAt() {
    }

    public static Instant of(LocalDate date, LocalTime todoTime) {
        if (date == null || todoTime == null) {
            return null;
        }
        return ZonedDateTime.of(date, todoTime, KST).toInstant();
    }
}
