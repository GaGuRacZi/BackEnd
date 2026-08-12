package com.gaguraczi.paw.domain.todo.dto.response;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;

import java.time.LocalDate;
import java.time.LocalTime;

public record TodoListResponse(
        Long todoId,
        String todo,
        String subTodo,
        LocalTime todoTime,
        LocalDate date,
        Long tagId,
        String tagName,
        TagColorEnum tagColorEnum,
        boolean routineEnabled,
        boolean completed
) {
    public static TodoListResponse from(TodoDateEntity todoDate) {
        TodoEntity todo = todoDate.getTodo();
        return new TodoListResponse(
                todo.getTodoId(),
                todo.getTodo(),
                todo.getSubTodo(),
                todo.getTodoTime(),
                todoDate.getDate(),
                todo.getTag().getTagId(),
                todo.getTag().getTagName(),
                todo.getTag().getTagColorEnum(),
                todo.isRoutineEnabled(),
                todoDate.isCompleted()
        );
    }
}