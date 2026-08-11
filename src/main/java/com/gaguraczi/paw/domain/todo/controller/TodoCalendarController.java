package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.response.TodoCalendarMonthResponse;
import com.gaguraczi.paw.domain.todo.exception.code.TodoSuccessCode;
import com.gaguraczi.paw.domain.todo.service.TodoCalendarService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/todos/calendar")
@Tag(name = "Todo Calendar", description = "월별 투두 캘린더 조회 API")
@RequiredArgsConstructor
public class TodoCalendarController {

    private final TodoCalendarService todoCalendarService;
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "월별 투두 캘린더 조회", description = "연/월 기준으로 투두 캘린더를 조회합니다.")
    public ApiResponse<TodoCalendarMonthResponse> getCalendarMonth(
            @Parameter(description = "조회 연도 (예: 2026)") @RequestParam int year,
            @Parameter(description = "조회 월 (1~12)") @RequestParam int month
    ) {
        UUID uid = securityUtils.currentUid();
        TodoCalendarMonthResponse response = todoCalendarService.getMonth(uid, year, month);
        return ApiResponse.onSuccess(TodoSuccessCode.TODO_CALENDAR_200, response);
    }
}
