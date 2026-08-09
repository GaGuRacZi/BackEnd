package com.gaguraczi.paw.domain.todo.scheduler;

import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.generator.RoutineTodoDateGenerator;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;


@Component
@Slf4j
@RequiredArgsConstructor
public class RoutineTodoScheduler {

    private final TodoRepository todoRepository;
    private final RoutineTodoDateGenerator routineTodoDateGenerator;

    @Scheduled(cron = "0 0 3 * * *")
    @Transactional
    public void generateRoutineTodoDates() {
        LocalDate today = LocalDate.now();
        List<TodoEntity> routineTodos = todoRepository.findAllByRoutineEnabledTrue();

        for (TodoEntity todo : routineTodos) {
            if (todo.getEndDate() != null && todo.getEndDate().isBefore(today)) {
                continue;
            }
            routineTodoDateGenerator.generate(todo, todo.getStartDate(), todo.getEndDate(), today);
        }
        log.info("routine todoDate batch done. targets={}", routineTodos.size());
    }
}