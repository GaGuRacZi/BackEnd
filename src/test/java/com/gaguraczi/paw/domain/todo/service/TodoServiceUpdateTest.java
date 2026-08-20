package com.gaguraczi.paw.domain.todo.service;

import com.gaguraczi.paw.domain.todo.dto.request.TodoUpdateRequest;
import com.gaguraczi.paw.domain.todo.entity.TagEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.repository.TagRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoDateRepository;
import com.gaguraczi.paw.domain.todo.repository.TodoRepository;
import com.gaguraczi.paw.domain.todo.support.TodoRemindAt;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoServiceUpdateTest {

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
        todoService = new TodoService(todoRepository, todoDateRepository, tagRepository, userRepository, clock);
    }

    @Test
    void 제목만_바꾸면_이미_claim된_같은_분_todo_date를_유지한다() {
        TagEntity tag = TagEntity.create(user, "약", TagColorEnum.RED);
        ReflectionTestUtils.setField(tag, "tagId", 1L);
        TodoEntity todo = TodoEntity.create(
                user, tag, "심장약", null, LocalTime.of(20, 0), true,
                LocalDate.of(2026, 8, 19), LocalDate.of(2026, 12, 31), WeekEnum.WED
        );
        ReflectionTestUtils.setField(todo, "todoId", 7L);
        when(todoRepository.findMyTodo(7L, uid)).thenReturn(Optional.of(todo));
        when(tagRepository.findByTagIdAndUser_Uid(1L, uid)).thenReturn(Optional.of(tag));

        TodoUpdateRequest req = new TodoUpdateRequest(
                "심장약 복용", null, 1L, LocalTime.of(20, 0), true,
                null, LocalDate.of(2026, 8, 19), LocalDate.of(2026, 12, 31), WeekEnum.WED
        );

        todoService.updateTodo(uid, 7L, req);

        verify(todoDateRepository, never())
                .deleteAllByTodo_TodoIdAndDateGreaterThanEqualAndCompletedFalse(anyLong(), any());
        verify(todoDateRepository, never()).findByTodo_TodoIdAndDate(any(), any());
        verify(todoDateRepository, never()).saveAndFlush(any());
    }
}
