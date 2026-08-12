package com.gaguraczi.paw.domain.todo.dto.response;

import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.TagColorEnum;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;

import java.time.LocalDate;
import java.time.LocalTime;


public record TodoDetailResponse(
        Long todoId,
        String todo,
        String subTodo,
        LocalTime todoTime,
        Long tagId,
        String tagName,
        TagColorEnum tagColorEnum,
        boolean routineEnabled,
        LocalDate startDate,
        LocalDate endDate,
        WeekEnum week
) {
    public static TodoDetailResponse from(TodoEntity todo) {
        return new TodoDetailResponse(
                todo.getTodoId(),
                todo.getTodo(),
                todo.getSubTodo(),
                todo.getTodoTime(),
                todo.getTag().getTagId(),
                todo.getTag().getTagName(),
                todo.getTag().getTagColorEnum(),
                todo.isRoutineEnabled(),
                todo.getStartDate(),
                todo.getEndDate(),
                todo.getWeek()
        );
    }
}