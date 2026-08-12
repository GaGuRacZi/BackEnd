package com.gaguraczi.paw.domain.todo.generator;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class RoutineTodoDateGenerator {

    private static final int MAX_GENERATE_YEARS = 1;

    private final TodoDateRepository todoDateRepository;


    public void generate(TodoEntity todo, LocalDate startDate, LocalDate endDate, LocalDate fromDate) {
        if (todo.getWeek() == null || endDate == null) {
            return;
        }

        LocalDate start = (startDate == null || startDate.isBefore(fromDate)) ? fromDate : startDate;
        LocalDate endLimit = start.plusYears(MAX_GENERATE_YEARS).minusDays(1);
        LocalDate end = endDate.isBefore(endLimit) ? endDate : endLimit;

        if (start.isAfter(end)) {
            return;
        }

        DayOfWeek dayOfWeek = todo.getWeek().toDayOfWeek();

        Set<LocalDate> existingDates = todoDateRepository
                .findAllByTodo_TodoIdAndDateBetween(todo.getTodoId(), start, end)
                .stream()
                .map(TodoDateEntity::getDate)
                .collect(Collectors.toSet());

        LocalDate cursor = start.with(java.time.temporal.TemporalAdjusters.nextOrSame(dayOfWeek));

        List<TodoDateEntity> todoDates = new ArrayList<>();
        for (LocalDate date = cursor; !date.isAfter(end); date = date.plusWeeks(1)) {
            if (existingDates.contains(date)) {
                continue;
            }
            todoDates.add(TodoDateEntity.create(todo, date));
        }

        if (!todoDates.isEmpty()) {
            todoDateRepository.saveAll(todoDates);
            log.debug("routine todoDate generated. todoId={}, count={}", todo.getTodoId(), todoDates.size());
        }
    }
}