package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.response.TodoCalendarMonthResponse;
import com.gaguraczi.paw.domain.todo.service.TodoCalendarService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo Calendar", description = "월별 투두 캘린더 조회 API")
@RequiredArgsConstructor

public class TodoCalendarController {

    public ApiResponse<TodoCalendarMonthResponse> getCalendarMonth(
            @RequestParam Long userId,
            @RequestParam int year,
            @RequestParam int month
    ) {
        TodoCalendarMonthResponse response =
                todoCalendarService.getMonthRemainingCounts(userId, year, month);
        return ApiResponse.onSuccess(SuccessCode.TODO_CALENDAR_MONTH_GET_SUCCESS, response);
    }
}
