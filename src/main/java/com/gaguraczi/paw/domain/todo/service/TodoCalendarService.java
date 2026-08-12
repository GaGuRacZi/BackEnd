package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.response.TodoCalendarMonthResponse;
import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.exception.code.TodoErrorCode;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.util.stream.Collectors.counting;
import static java.util.stream.Collectors.groupingBy;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoCalendarService {

    private final TodoDateRepository todoDateRepository;


    public TodoCalendarMonthResponse getMonth(UUID uid, int year, int month) {
        YearMonth yearMonth = toYearMonth(year, month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        List<TodoDateEntity> todoDates =
                todoDateRepository.findAllByTodo_User_UidAndDateBetween(uid, startDate, endDate);

        Map<LocalDate, Long> totalByDate = todoDates.stream()
                .collect(groupingBy(TodoDateEntity::getDate, counting()));

        Map<LocalDate, Long> completedByDate = todoDates.stream()
                .filter(TodoDateEntity::isCompleted)
                .collect(groupingBy(TodoDateEntity::getDate, counting()));

        List<TodoCalendarMonthResponse.DayInfo> days = new ArrayList<>();
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            LocalDate date = yearMonth.atDay(day);
            long total = totalByDate.getOrDefault(date, 0L);
            long completed = completedByDate.getOrDefault(date, 0L);
            days.add(new TodoCalendarMonthResponse.DayInfo(
                    date, total, completed, total - completed, total > 0
            ));
        }

        long monthTotal = todoDates.size();
        long monthCompleted = todoDates.stream().filter(TodoDateEntity::isCompleted).count();

        return TodoCalendarMonthResponse.of(year, month, monthTotal, monthCompleted, days);
    }


    private YearMonth toYearMonth(int year, int month) {
        try {
            return YearMonth.of(year, month);
        } catch (DateTimeException e) {
            throw GeneralException.of(TodoErrorCode.TODO_CALENDAR_PARAM_400_8, e);
        }
    }
}