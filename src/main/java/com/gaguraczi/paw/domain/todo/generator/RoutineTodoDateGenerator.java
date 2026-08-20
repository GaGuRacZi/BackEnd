package com.gaguraczi.paw.domain.todo.generator;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RoutineTodoDateGenerator {

    private final TodoDateRepository todoDateRepository;

    public void generate(TodoEntity todo, LocalDate today) {
        if (todo.getTodoId() == null
                || !todo.isRoutineEnabled()
                || todo.getWeek() == null
                || todo.getStartDate() == null
                || todo.getEndDate() == null) {
            return;
        }

        LocalDate from = todo.getStartDate().isAfter(today) ? todo.getStartDate() : today;
        LocalDate to = todo.getEndDate();
        if (from.isAfter(to)) {
            return;
        }

        DayOfWeek target = todo.getWeek().toDayOfWeek();
        int daysToAdd = (target.getValue() - from.getDayOfWeek().getValue() + 7) % 7;
        LocalDate cursor = from.plusDays(daysToAdd);
        if (cursor.isAfter(to)) {
            return;
        }

        Set<LocalDate> existing = todoDateRepository
                .findAllByTodo_TodoIdAndDateBetween(todo.getTodoId(), from, to)
                .stream()
                .map(TodoDateEntity::getDate)
                .collect(Collectors.toSet());

        List<TodoDateEntity> toSave = new ArrayList<>();
        while (!cursor.isAfter(to)) {
            if (!existing.contains(cursor)) {
                toSave.add(TodoDateEntity.create(todo, cursor));
            }
            cursor = cursor.plusWeeks(1);
        }

        if (!toSave.isEmpty()) {
            todoDateRepository.saveAll(toSave);
        }
    }
}
