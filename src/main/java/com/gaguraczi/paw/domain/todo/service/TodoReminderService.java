package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TodoReminderService {

    public static final int PAGE_SIZE = 200;

    private final TodoRepository todoRepository;
    private final TodoDateRepository todoDateRepository;

    @Transactional
    public void upsertDueRoutineDates(
            LocalDate today,
            WeekEnum week,
            LocalTime fromTime,
            LocalTime toTime,
            boolean wrapsMidnight
    ) {
        List<TodoEntity> routines = todoRepository.findRoutinesDueThisMinute(
                week, today, fromTime, toTime, wrapsMidnight);
        for (TodoEntity todo : routines) {
            if (todoDateRepository.findByTodo_TodoIdAndDate(todo.getTodoId(), today).isPresent()) {
                continue;
            }
            try {
                todoDateRepository.saveAndFlush(TodoDateEntity.create(todo, today));
            } catch (DataIntegrityViolationException e) {
                log.debug("Routine todo_date already exists todoId={} date={}", todo.getTodoId(), today);
            }
        }
    }

    @Transactional(readOnly = true)
    public List<TodoDateEntity> findDue(Instant from, Instant to, long afterId) {
        return todoDateRepository.findDueReminders(from, to, afterId, PageRequest.of(0, PAGE_SIZE));
    }

    @Transactional
    public boolean claim(Long todoDateId, Instant now) {
        return todoDateRepository.markNotifiedIfPending(todoDateId, now) == 1;
    }
}
