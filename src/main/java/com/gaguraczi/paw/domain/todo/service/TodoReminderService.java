package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TodoReminderService {

    public static final int PAGE_SIZE = 200;
    static final Duration CLAIM_LEASE = Duration.ofMinutes(2);

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
        if (routines.isEmpty()) {
            return;
        }
        List<Long> ids = routines.stream().map(TodoEntity::getTodoId).toList();
        todoDateRepository.insertIgnoreDates(ids, today);
    }

    @Transactional(readOnly = true)
    public List<TodoDateEntity> findDue(Instant from, Instant to, long afterId, Instant now) {
        return todoDateRepository.findDueReminders(from, to, afterId, now, PageRequest.of(0, PAGE_SIZE));
    }

    @Transactional
    public boolean claim(Long todoDateId, Instant now) {
        return todoDateRepository.claimProcessing(todoDateId, now.plus(CLAIM_LEASE), now) == 1;
    }

    @Transactional
    public void complete(Long todoDateId, Instant now) {
        todoDateRepository.markNotified(todoDateId, now);
    }

    @Transactional
    public void release(Long todoDateId) {
        todoDateRepository.releaseClaim(todoDateId);
    }
}
