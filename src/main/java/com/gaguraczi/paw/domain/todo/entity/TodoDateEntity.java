package com.gaguraczi.paw.domain.todo.entity;

import com.gaguraczi.paw.domain.todo.support.TodoRemindAt;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "todo_date",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_todo_date",
                columnNames = {"todo_id", "date"}
        ),
        indexes = {
                @Index(name = "idx_todo_date_date", columnList = "date"),
                @Index(name = "idx_todo_date_remind_at", columnList = "remind_at")
        }
)
public class TodoDateEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_date_id")
    private Long todoDateId;

    @Column(name = "date", nullable = false)
    private LocalDate date;

    @Column(name = "completed", nullable = false)
    private boolean completed = false;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "remind_at", columnDefinition = "timestamptz")
    private Instant remindAt;

    @Column(name = "notified_at", columnDefinition = "timestamptz")
    private Instant notifiedAt;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    private TodoEntity todo;

    public static TodoDateEntity create(TodoEntity todo, LocalDate date) {
        TodoDateEntity todoDate = new TodoDateEntity();
        todoDate.todo = todo;
        todoDate.date = date;
        todoDate.completed = false;
        todoDate.completedAt = null;
        todoDate.remindAt = TodoRemindAt.of(date, todo.getTodoTime());
        todoDate.notifiedAt = null;
        return todoDate;
    }

    public void changeCompleted(boolean completed) {
        this.completed = completed;
        this.completedAt = completed ? LocalDateTime.now() : null;
    }

    public void changeDate(LocalDate date) {
        this.date = date;
        refreshSchedule();
    }

    public void refreshSchedule() {
        this.remindAt = TodoRemindAt.of(this.date, this.todo.getTodoTime());
        this.notifiedAt = null;
    }
}
