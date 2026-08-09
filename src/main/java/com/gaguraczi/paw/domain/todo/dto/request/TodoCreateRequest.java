package com.gaguraczi.paw.domain.todo.dto.request;

import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record TodoCreateRequest(

        @NotBlank(message = "할 일 입력은 필수입니다.")
        @Size(max = 500, message = "할 일은 500자 이하여야 합니다.")
        String todo,

        @Size(max = 1000, message = "세부 내용은 1000자 이하여야 합니다.")
        String subTodo,

        @NotNull(message = "태그 선택은 필수입니다.")
        Long tagId,

        LocalTime todoTime,

        boolean routineEnabled,


        LocalDate date,

        LocalDate startDate,
        LocalDate endDate,
        WeekEnum week
) {
    public TodoCreateRequest {
        todo = (todo == null) ? null : todo.trim();
        subTodo = (subTodo == null || subTodo.isBlank()) ? null : subTodo.trim();
    }
}