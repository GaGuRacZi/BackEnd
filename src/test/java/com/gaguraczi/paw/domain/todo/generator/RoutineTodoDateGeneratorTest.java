package com.gaguraczi.paw.domain.todo.generator;

import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RoutineTodoDateGeneratorTest {

    @Mock
    private TodoDateRepository todoDateRepository;

    private RoutineTodoDateGenerator generator;
    private final User user = User.builder().uid(UUID.randomUUID()).build();
    private final TagEntity tag = TagEntity.create(user, "약", TagColorEnum.RED);
    private final LocalDate today = LocalDate.of(2026, 8, 19);

    @BeforeEach
    void setUp() {
        generator = new RoutineTodoDateGenerator(todoDateRepository);
    }

    @Test
    void 오늘_요일이_아니어도_해당_요일_날짜를_저장한다() {
        TodoEntity todo = routine(WeekEnum.FRI, today, LocalDate.of(2026, 9, 4));
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(7L), any(), any())).thenReturn(List.of());

        generator.generate(todo, today);

        List<LocalDate> dates = capturedDates();
        assertThat(dates).containsExactly(
                LocalDate.of(2026, 8, 21),
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 9, 4)
        );
        assertThat(dates).allMatch(d -> d.getDayOfWeek() == DayOfWeek.FRIDAY);
    }

    @Test
    void 이미_있는_날짜는_건너뛴다() {
        TodoEntity todo = routine(WeekEnum.FRI, today, LocalDate.of(2026, 9, 4));
        TodoDateEntity existing = TodoDateEntity.create(todo, LocalDate.of(2026, 8, 21));
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(7L), any(), any()))
                .thenReturn(List.of(existing));

        generator.generate(todo, today);

        assertThat(capturedDates()).containsExactly(
                LocalDate.of(2026, 8, 28),
                LocalDate.of(2026, 9, 4)
        );
    }

    @Test
    void start_end_구간만_생성하고_끝날짜까지_채운다() {
        LocalDate end = LocalDate.of(2026, 10, 7);
        TodoEntity todo = routine(WeekEnum.WED, today, end);
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(7L), any(), any())).thenReturn(List.of());

        generator.generate(todo, today);

        List<LocalDate> dates = capturedDates();
        assertThat(dates).containsExactly(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 16),
                LocalDate.of(2026, 9, 23),
                LocalDate.of(2026, 9, 30),
                LocalDate.of(2026, 10, 7)
        );
        assertThat(dates).allMatch(d -> !d.isBefore(today) && !d.isAfter(end));
    }

    @Test
    void 시작일이_미래면_그_전은_만들지_않는다() {
        TodoEntity todo = routine(WeekEnum.WED, LocalDate.of(2026, 9, 2), LocalDate.of(2026, 9, 16));
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(7L), any(), any())).thenReturn(List.of());

        generator.generate(todo, today);

        assertThat(capturedDates()).containsExactly(
                LocalDate.of(2026, 9, 2),
                LocalDate.of(2026, 9, 9),
                LocalDate.of(2026, 9, 16)
        );
    }

    @Test
    void 시작일이_과거면_오늘_이전은_만들지_않는다() {
        TodoEntity todo = routine(WeekEnum.WED, LocalDate.of(2026, 8, 1), LocalDate.of(2026, 9, 2));
        when(todoDateRepository.findAllByTodo_TodoIdAndDateBetween(eq(7L), any(), any())).thenReturn(List.of());

        generator.generate(todo, today);

        assertThat(capturedDates()).containsExactly(
                LocalDate.of(2026, 8, 19),
                LocalDate.of(2026, 8, 26),
                LocalDate.of(2026, 9, 2)
        );
    }

    @Test
    void 루틴이_아니면_저장하지_않는다() {
        TodoEntity todo = TodoEntity.create(
                user, tag, "일회", null, LocalTime.of(20, 0), false,
                null, null, null
        );
        ReflectionTestUtils.setField(todo, "todoId", 7L);

        generator.generate(todo, today);

        verify(todoDateRepository, never()).findAllByTodo_TodoIdAndDateBetween(any(), any(), any());
        verify(todoDateRepository, never()).saveAll(any());
    }

    private TodoEntity routine(WeekEnum week, LocalDate start, LocalDate end) {
        TodoEntity todo = TodoEntity.create(
                user, tag, "심장약", null, LocalTime.of(20, 0), true, start, end, week
        );
        ReflectionTestUtils.setField(todo, "todoId", 7L);
        return todo;
    }

    @SuppressWarnings("unchecked")
    private List<LocalDate> capturedDates() {
        ArgumentCaptor<List<TodoDateEntity>> captor = ArgumentCaptor.forClass(List.class);
        verify(todoDateRepository).saveAll(captor.capture());
        return captor.getValue().stream().map(TodoDateEntity::getDate).toList();
    }
}
