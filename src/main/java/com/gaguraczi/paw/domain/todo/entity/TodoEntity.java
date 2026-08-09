package com.gaguraczi.paw.domain.todo.entity;

import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "todo",
        indexes = {
                @Index(name = "idx_todo_uid", columnList = "uid"),
                @Index(name = "idx_todo_tag", columnList = "tag_id")
        }
)
public class TodoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;


    @Column(name = "todo", nullable = false, columnDefinition = "TEXT")
    private String todo;


    @Column(name = "sub_todo", columnDefinition = "TEXT")
    private String subTodo;

    @Column(name = "todo_time")
    private LocalTime todoTime;


    @Column(name = "routine_enabled", nullable = false)
    private boolean routineEnabled;

    @Column(name = "start_date")
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "week", length = 10)
    private WeekEnum week;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tag;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @OneToMany(mappedBy = "todo", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TodoDateEntity> todoDates = new ArrayList<>();

    public static TodoEntity create(User user,
                                    TagEntity tag,
                                    String todo,
                                    String subTodo,
                                    LocalTime todoTime,
                                    boolean routineEnabled,
                                    LocalDate startDate,
                                    LocalDate endDate,
                                    WeekEnum week) {
        TodoEntity entity = new TodoEntity();
        entity.user = user;
        entity.tag = tag;
        entity.todo = todo;
        entity.subTodo = subTodo;
        entity.todoTime = todoTime;
        entity.routineEnabled = routineEnabled;
        entity.startDate = startDate;
        entity.endDate = endDate;
        entity.week = week;
        return entity;
    }

    public void update(TagEntity tag,
                       String todo,
                       String subTodo,
                       LocalTime todoTime,
                       LocalDate startDate,
                       LocalDate endDate,
                       WeekEnum week) {
        this.tag = tag;
        this.todo = todo;
        this.subTodo = subTodo;
        this.todoTime = todoTime;
        this.startDate = startDate;
        this.endDate = endDate;
        this.week = week;
    }

    public boolean isOwnedBy(java.util.UUID uid) {
        return this.user.getUid().equals(uid);
    }
}