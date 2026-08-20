package com.gaguraczi.paw.domain.todo.scheduler;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.todo.fcm.TodoFcmService;
import com.gaguraczi.paw.domain.todo.service.TodoReminderService;
import com.gaguraczi.paw.domain.todo.support.TodoRemindAt;
import com.gaguraczi.paw.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TodoReminderScheduler {

    static final long LOCK_TTL_SECONDS = 70;
    private static final DateTimeFormatter KEY_MINUTE = DateTimeFormatter.ofPattern("yyyyMMddHHmm");

    private final RedisUtil redisUtil;
    private final TodoReminderService todoReminderService;
    private final TodoFcmService todoFcmService;
    private final Clock clock;

    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
    public void tick() {
        ZonedDateTime minute = currentMinute();
        String key = "todo:reminder:" + minute.format(KEY_MINUTE);
        try {
            if (!redisUtil.setIfAbsent(key, "1", LOCK_TTL_SECONDS)) {
                return;
            }
        } catch (Exception e) {
            log.warn("Todo reminder lock failed: {}", e.getMessage());
            return;
        }
        try {
            processMinute(minute);
        } catch (Exception e) {
            log.warn("Todo reminder run failed: {}", e.getMessage());
        }
    }

    void processMinute(ZonedDateTime minute) {
        Instant from = minute.toInstant();
        Instant to = minute.plusMinutes(1).toInstant();
        LocalDate today = minute.toLocalDate();
        LocalTime fromTime = minute.toLocalTime();
        LocalTime toTime = fromTime.plusMinutes(1);
        boolean wrapsMidnight = fromTime.equals(LocalTime.of(23, 59));
        WeekEnum week = WeekEnum.from(today.getDayOfWeek());

        todoReminderService.upsertDueRoutineDates(today, week, fromTime, toTime, wrapsMidnight);

        long afterId = 0L;
        while (true) {
            List<TodoDateEntity> batch = todoReminderService.findDue(from, to, afterId);
            if (batch.isEmpty()) {
                break;
            }
            for (TodoDateEntity todoDate : batch) {
                if (todoReminderService.claim(todoDate.getTodoDateId(), Instant.now(clock))) {
                    todoFcmService.sendReminder(todoDate);
                }
            }
            afterId = batch.getLast().getTodoDateId();
            if (batch.size() < TodoReminderService.PAGE_SIZE) {
                break;
            }
        }
    }

    private ZonedDateTime currentMinute() {
        return ZonedDateTime.now(clock).withZoneSameInstant(TodoRemindAt.KST)
                .truncatedTo(ChronoUnit.MINUTES);
    }
}
