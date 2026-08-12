package com.gaguraczi.paw.domain.todo.enums;

import java.time.DayOfWeek;

public enum WeekEnum {

    MON(DayOfWeek.MONDAY),
    TUE(DayOfWeek.TUESDAY),
    WED(DayOfWeek.WEDNESDAY),
    THU(DayOfWeek.THURSDAY),
    FRI(DayOfWeek.FRIDAY),
    SAT(DayOfWeek.SATURDAY),
    SUN(DayOfWeek.SUNDAY);

    private final DayOfWeek dayOfWeek;

    WeekEnum(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public DayOfWeek toDayOfWeek() {
        return dayOfWeek;
    }

    public static WeekEnum from(DayOfWeek dayOfWeek) {
        for (WeekEnum week : values()) {
            if (week.dayOfWeek == dayOfWeek) {
                return week;
            }
        }
        throw new IllegalArgumentException("Unknown DayOfWeek: " + dayOfWeek);
    }
}