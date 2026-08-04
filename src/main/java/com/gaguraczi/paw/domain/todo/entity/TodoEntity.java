package com.gaguraczi.paw.domain.todo.entity;

import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "todo")
public class TodoEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "todo_id")
    private Long todoId;

    @Column(name = "todo", columnDefinition = "TEXT", nullable = false)
    private String todo;

    @Column(name = "sub_todo", columnDefinition = "TEXT")
    private String subTodo;

    @Column(name = "todo_time", nullable = false)
    private LocalDateTime todoTime;

    @ManyToOne(fetch = FetchType.LAZY)

    @JoinColumn(name = "tag_id", nullable = false)
    private TagEntity tag;
}