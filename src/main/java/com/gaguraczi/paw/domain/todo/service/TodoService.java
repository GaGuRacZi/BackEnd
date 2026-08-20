package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TodoCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TodoUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TodoDetailResponse;
import com.gaguraczi.paw.domain.todo.dto.response.TodoListResponse;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.exception.code.TodoErrorCode;
import com.gaguraczi.paw.domain.todo.repository.TagRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TodoService {

    private final TodoRepository todoRepository;
    private final TodoDateRepository todoDateRepository;
    private final TagRepository tagRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    private static final Comparator<TodoDateEntity> LIST_ORDER =
            Comparator.comparing(
                            (TodoDateEntity td) -> td.getTodo().getTodoTime(),
                            Comparator.nullsLast(Comparator.naturalOrder()))
                    .thenComparing(td -> td.getTodo().getTodoId());


    public List<TodoListResponse> getTodosByDate(UUID uid, LocalDate date, Long tagId) {
        List<TodoDateEntity> todoDates = (tagId == null)
                ? todoDateRepository.findAllByTodo_User_UidAndDate(uid, date)
                : todoDateRepository.findAllByTodo_User_UidAndDateAndTodo_Tag_TagId(uid, date, tagId);

        return todoDates.stream()
                .sorted(LIST_ORDER)
                .map(TodoListResponse::from)
                .toList();
    }


    public TodoDetailResponse getTodoDetail(UUID uid, Long todoId) {
        return TodoDetailResponse.from(getMyTodoOrThrow(uid, todoId));
    }


    @Transactional
    public TodoDetailResponse createTodo(UUID uid, TodoCreateRequest request) {
        User user = userRepository.getReferenceById(uid);
        TagEntity tag = getMyTagOrThrow(uid, request.tagId());

        boolean routineEnabled = request.routineEnabled();

        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;

        if (routineEnabled) {
            startDate = (request.startDate() != null) ? request.startDate() : LocalDate.now(clock);
            endDate = request.endDate();
            week = request.week();
            validateRoutine(startDate, endDate, week);
        } else if (request.date() == null) {
            throw new GeneralException(TodoErrorCode.TODO_DATE_REQUIRED_400_2);
        }

        TodoEntity todo = TodoEntity.create(
                user, tag,
                request.todo(), request.subTodo(), request.todoTime(),
                routineEnabled, startDate, endDate, week
        );

        try {
            todo = todoRepository.saveAndFlush(todo);
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(TodoErrorCode.TODO_CREATE_400_1);
        }

        LocalDate today = LocalDate.now(clock);
        if (!routineEnabled) {
            saveSingleDate(todo, request.date());
        } else if (matchesToday(todo, today)) {
            upsertDate(todo, today);
        }

        return TodoDetailResponse.from(todo);
    }

    @Transactional
    public TodoDetailResponse updateTodo(UUID uid, Long todoId, TodoUpdateRequest request) {
        TodoEntity todo = getMyTodoOrThrow(uid, todoId);
        TagEntity tag = getMyTagOrThrow(uid, request.tagId());

        boolean wasRoutine = todo.isRoutineEnabled();
        boolean nowRoutine = (request.routineEnabled() != null) ? request.routineEnabled() : wasRoutine;
        LocalTime previousTime = todo.getTodoTime();
        LocalDate previousStart = todo.getStartDate();
        LocalDate previousEnd = todo.getEndDate();
        WeekEnum previousWeek = todo.getWeek();

        if (wasRoutine != nowRoutine) {
            throw new GeneralException(TodoErrorCode.TODO_ROUTINE_TYPE_CHANGE_400_6);
        }

        LocalDate startDate = null;
        LocalDate endDate = null;
        WeekEnum week = null;

        if (nowRoutine) {

            startDate = (request.startDate() != null) ? request.startDate() : todo.getStartDate();
            endDate = request.endDate();
            week = request.week();
            validateRoutine(startDate, endDate, week);
        } else if (request.date() == null) {
            throw new GeneralException(TodoErrorCode.TODO_DATE_REQUIRED_400_2);
        }

        LocalTime todoTime = request.todoTime();
        todo.update(tag, request.todo(), request.subTodo(), todoTime, nowRoutine, startDate, endDate, week);
        todoRepository.flush();

        LocalDate today = LocalDate.now(clock);
        boolean timeChanged = !Objects.equals(previousTime, todoTime);

        if (nowRoutine) {
            boolean scheduleChanged = !Objects.equals(previousTime, todoTime)
                    || !Objects.equals(previousStart, startDate)
                    || !Objects.equals(previousEnd, endDate)
                    || !Objects.equals(previousWeek, week);
            if (scheduleChanged) {
                todoDateRepository.deleteAllByTodo_TodoIdAndDateGreaterThanEqualAndCompletedFalse(todoId, today);
                todoDateRepository.flush();

                deleteStaleDates(todoId, startDate, endDate, week);
                if (matchesToday(todo, today)) {
                    upsertDate(todo, today);
                }
            }
        } else {
            TodoDateEntity todoDate = todoDateRepository.findAllByTodo_TodoIdOrderByDateAsc(todoId).stream()
                    .findFirst()
                    .orElseThrow(() -> new GeneralException(TodoErrorCode.TODO_DATE_GET_404_2));
            boolean dateChanged = !todoDate.getDate().equals(request.date());
            if (dateChanged) {
                todoDate.changeDate(request.date());
            } else if (timeChanged) {
                todoDate.refreshSchedule();
            }
        }

        return TodoDetailResponse.from(todo);
    }

    @Transactional
    public TodoListResponse updateComplete(UUID uid, Long todoId, LocalDate date, boolean completed) {
        getMyTodoOrThrow(uid, todoId);


        if (date == null) {
            throw new GeneralException(TodoErrorCode.TODO_DATE_REQUIRED_400_2);
        }

        TodoDateEntity todoDate = todoDateRepository.findByTodo_TodoIdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(TodoErrorCode.TODO_DATE_GET_404_2));

        if (todoDate.isCompleted() != completed) {
            todoDate.changeCompleted(completed, LocalDateTime.now(clock));
        }

        return TodoListResponse.from(todoDate);
    }


    @Transactional
    public void deleteTodo(UUID uid, Long todoId, LocalDate date, boolean deleteAll) {
        TodoEntity todo = getMyTodoOrThrow(uid, todoId);

        if (!todo.isRoutineEnabled() || deleteAll) {
            todoRepository.delete(todo);
            return;
        }


        if (date == null) {
            throw new GeneralException(TodoErrorCode.TODO_DATE_REQUIRED_400_2);
        }

        TodoDateEntity todoDate = todoDateRepository.findByTodo_TodoIdAndDate(todoId, date)
                .orElseThrow(() -> new GeneralException(TodoErrorCode.TODO_DELETE_404_4));
        todoDateRepository.delete(todoDate);
    }


    private TodoEntity getMyTodoOrThrow(UUID uid, Long todoId) {
        return todoRepository.findMyTodo(todoId, uid)
                .orElseThrow(() -> new GeneralException(TodoErrorCode.TODO_GET_404_1));
    }

    private TagEntity getMyTagOrThrow(UUID uid, Long tagId) {
        return tagRepository.findByTagIdAndUser_Uid(tagId, uid)
                .orElseThrow(() -> new GeneralException(TodoErrorCode.TODO_TAG_404_3));
    }

    private void validateRoutine(LocalDate startDate, LocalDate endDate, WeekEnum week) {
        if (endDate == null) {
            throw new GeneralException(TodoErrorCode.TODO_ROUTINE_END_DATE_400_3);
        }
        if (week == null) {
            throw new GeneralException(TodoErrorCode.TODO_ROUTINE_WEEK_400_4);
        }
        if (startDate != null && startDate.isAfter(endDate)) {
            throw new GeneralException(TodoErrorCode.TODO_ROUTINE_RANGE_400_5);
        }
    }

    static boolean matchesToday(TodoEntity todo, LocalDate today) {
        if (!todo.isRoutineEnabled() || todo.getWeek() == null || todo.getStartDate() == null || todo.getEndDate() == null) {
            return false;
        }
        if (today.isBefore(todo.getStartDate()) || today.isAfter(todo.getEndDate())) {
            return false;
        }
        return today.getDayOfWeek() == todo.getWeek().toDayOfWeek();
    }

    private void deleteStaleDates(Long todoId, LocalDate startDate, LocalDate endDate, WeekEnum week) {
        DayOfWeek dayOfWeek = week.toDayOfWeek();

        List<TodoDateEntity> stale = todoDateRepository.findAllByTodo_TodoIdOrderByDateAsc(todoId).stream()
                .filter(td -> !td.isCompleted())
                .filter(td -> td.getDate().isBefore(startDate)
                        || td.getDate().isAfter(endDate)
                        || td.getDate().getDayOfWeek() != dayOfWeek)
                .toList();

        if (!stale.isEmpty()) {
            todoDateRepository.deleteAll(stale);
            todoDateRepository.flush();
        }
    }

    private void saveSingleDate(TodoEntity todo, LocalDate date) {
        try {
            todoDateRepository.saveAndFlush(TodoDateEntity.create(todo, date));
        } catch (DataIntegrityViolationException e) {
            throw new GeneralException(TodoErrorCode.TODO_CREATE_400_1);
        }
    }

    private void upsertDate(TodoEntity todo, LocalDate date) {
        todoDateRepository.findByTodo_TodoIdAndDate(todo.getTodoId(), date)
                .ifPresentOrElse(TodoDateEntity::refreshSchedule, () -> {
                    try {
                        todoDateRepository.saveAndFlush(TodoDateEntity.create(todo, date));
                    } catch (DataIntegrityViolationException ignored) {
                        todoDateRepository.findByTodo_TodoIdAndDate(todo.getTodoId(), date)
                                .ifPresent(TodoDateEntity::refreshSchedule);
                    }
                });
    }
}
