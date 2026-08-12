package com.gaguraczi.paw.domain.todo.dto.response;

import java.time.LocalDate;
import java.util.List;

public record TodoCalendarMonthResponse(
        int year,
        int month,
        long monthTotalCount,
        long monthCompletedCount,
        List<DayInfo> days
) {
    public static TodoCalendarMonthResponse of(int year,
                                               int month,
                                               long monthTotalCount,
                                               long monthCompletedCount,
                                               List<DayInfo> days) {
        return new TodoCalendarMonthResponse(year, month, monthTotalCount, monthCompletedCount, days);
    }

    public record DayInfo(
            LocalDate date,
            long totalCount,
            long completedCount,
            long remainingCount,
            boolean hasTodo
    ) {
    }
}