package com.gaguraczi.paw.domain.todo.scheduler;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.fcm.TodoFcmService;
import com.gaguraczi.paw.domain.todo.service.TodoReminderService;
import com.gaguraczi.paw.domain.todo.support.TodoRemindAt;
import com.gaguraczi.paw.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TodoReminderSchedulerTest {

    @Mock
    private RedisUtil redisUtil;
    @Mock
    private TodoReminderService todoReminderService;
    @Mock
    private TodoFcmService todoFcmService;

    private TodoReminderScheduler scheduler;

    @BeforeEach
    void setUp() {
        Instant instant = ZonedDateTime.of(2026, 8, 19, 20, 0, 0, 0, TodoRemindAt.KST).toInstant();
        Clock clock = Clock.fixed(instant, TodoRemindAt.KST);
        scheduler = new TodoReminderScheduler(redisUtil, todoReminderService, todoFcmService, clock);
    }

    @Test
    void 락을_못_잡으면_조회하지_않는다() {
        when(redisUtil.setIfAbsent(eq("todo:reminder:202608192000"), eq("1"), eq(70L))).thenReturn(false);

        scheduler.tick();

        verify(todoReminderService, never()).findDue(any(), any(), anyLong());
        verify(todoFcmService, never()).sendReminder(any());
    }

    @Test
    void 시각이_맞고_claim되면_발송한다() {
        when(redisUtil.setIfAbsent(eq("todo:reminder:202608192000"), eq("1"), eq(70L))).thenReturn(true);
        TodoDateEntity due = mock(TodoDateEntity.class);
        when(due.getTodoDateId()).thenReturn(15L);
        Instant from = ZonedDateTime.of(2026, 8, 19, 20, 0, 0, 0, TodoRemindAt.KST).toInstant();
        Instant to = from.plusSeconds(60);
        when(todoReminderService.findDue(from, to, 0L)).thenReturn(List.of(due));
        when(todoReminderService.claim(eq(15L), any())).thenReturn(true);

        scheduler.tick();

        verify(todoReminderService).upsertDueRoutineDates(
                eq(LocalDate.of(2026, 8, 19)),
                any(),
                eq(LocalTime.of(20, 0)),
                eq(LocalTime.of(20, 1)),
                eq(false)
        );
        verify(todoFcmService).sendReminder(due);
    }

    @Test
    void claim_실패면_중복_발송하지_않는다() {
        Instant from = ZonedDateTime.of(2026, 8, 19, 20, 0, 0, 0, TodoRemindAt.KST).toInstant();
        ZonedDateTime minute = from.atZone(TodoRemindAt.KST);
        TodoDateEntity due = mock(TodoDateEntity.class);
        when(due.getTodoDateId()).thenReturn(15L);
        when(todoReminderService.findDue(any(), any(), eq(0L))).thenReturn(List.of(due));
        when(todoReminderService.claim(eq(15L), any())).thenReturn(false);

        scheduler.processMinute(minute);

        verify(todoFcmService, never()).sendReminder(any());
    }
}
