package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TodoCreateRequest;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.generator.RoutineTodoDateGenerator;
import com.gaguraczi.paw.domain.todo.repository.TagRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import com.gaguraczi.paw.domain.todo.support.TodoRemindAt;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceCreateTest {

    @Mock
    private TodoRepository todoRepository;
    @Mock
    private TodoDateRepository todoDateRepository;
    @Mock
    private TagRepository tagRepository;
    @Mock
    private UserRepository userRepository;

    private TodoService todoService;
    private final UUID uid = UUID.randomUUID();
    private final User user = User.builder().uid(uid).build();
    private final Clock clock = Clock.fixed(
            ZonedDateTime.of(2026, 8, 19, 10, 0, 0, 0, TodoRemindAt.KST).toInstant(),
            TodoRemindAt.KST
    );

    @BeforeEach
    void setUp() {
        RoutineTodoDateGenerator generator = new RoutineTodoDateGenerator(todoDateRepository);
        todoService = new TodoService(
                todoRepository, todoDateRepository, tagRepository, userRepository, generator, clock);
    }

    @Test
    void 일회_할_일은_todo_date와_remind_at을_저장한다() {
        TagEntity tag = TagEntity.create(user, "약", TagColorEnum.RED);
        when(userRepository.getReferenceById(uid)).thenReturn(user);
        when(tagRepository.findByTagIdAndUser_Uid(1L, uid)).thenReturn(Optional.of(tag));
        when(todoRepository.saveAndFlush(any(TodoEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(todoDateRepository.saveAndFlush(any(TodoDateEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TodoCreateRequest req = new TodoCreateRequest(
                "심장약 복용", null, 1L, LocalTime.of(20, 0), false,
                LocalDate.of(2026, 8, 19), null, null, null
        );

        todoService.createTodo(uid, req);

        ArgumentCaptor<TodoDateEntity> captor = ArgumentCaptor.forClass(TodoDateEntity.class);
        verify(todoDateRepository).saveAndFlush(captor.capture());
        TodoDateEntity saved = captor.getValue();
        assertThat(saved.getDate()).isEqualTo(LocalDate.of(2026, 8, 19));
        assertThat(saved.getRemindAt()).isEqualTo(TodoRemindAt.of(LocalDate.of(2026, 8, 19), LocalTime.of(20, 0)));
        assertThat(saved.getNotifiedAt()).isNull();
        assertThat(saved.isCompleted()).isFalse();
        verify(todoDateRepository, never()).saveAll(any());
    }

    @Test
    void 루틴은_오늘_요일이_아니어도_해당_요일_todo_date를_여러_건_저장한다() {
        TagEntity tag = TagEntity.create(user, "약", TagColorEnum.RED);
        when(userRepository.getReferenceById(uid)).thenReturn(user);
        when(tagRepository.findByTagIdAndUser_Uid(1L, uid)).thenReturn(Optional.of(tag));
        when(todoRepository.saveAndFlush(any(TodoEntity.class))).thenAnswer(invocation -> {
            TodoEntity todo = invocation.getArgument(0);
            ReflectionTestUtils.setField(todo, "todoId", 10L);
            return todo;
        });
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(10L), any(), any())).thenReturn(List.of());

        TodoCreateRequest req = new TodoCreateRequest(
                "심장약 복용", null, 1L, LocalTime.of(20, 0), true,
                null, LocalDate.of(2026, 8, 19), LocalDate.of(2026, 9, 4), WeekEnum.FRI
        );

        todoService.createTodo(uid, req);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<TodoDateEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(todoDateRepository).saveAll(captor.capture());
        List<LocalDate> dates = captor.getValue().stream().map(TodoDateEntity::getDate).toList();
        assertThat(dates).containsExactly(
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 9, 4)
        );
        assertThat(dates).allMatch(d -> d.getDayOfWeek() == DayOfWeek.FRIDAY);
        assertThat(captor.getValue()).allSatisfy(td -> {
            assertThat(td.getRemindAt()).isEqualTo(TodoRemindAt.of(td.getDate(), LocalTime.of(20, 0)));
            assertThat(td.isCompleted()).isFalse();
        });
        verify(todoDateRepository, never()).saveAndFlush(any());
    }
}
