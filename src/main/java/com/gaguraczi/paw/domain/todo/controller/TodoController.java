package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.response.TodoListResponse;
import com.gaguraczi.paw.domain.todo.service.TodoService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/todos")
@Tag(name = "Todo", description = "투두 생성, 조회, 수정, 삭제 및 완료 처리 API")
@RequiredArgsConstructor
public class TodoController {

    private final TodoService todoService;

    @GetMapping
    @Operation(summary = "날짜별 투두 목록 조회",
            description = "선택한 날짜의 투두 목록을 조회합니다. tagId를 주면 해당 태그만 필터링합니다.")
    public ApiResponse<List<TodoListResponse>> getTodosByDate(
            @Parameter(description = "사용자 UUID") @RequestParam UUID uid,
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "태그 필터 (선택)") @RequestParam(required = false) Long tagId
    ) {
        return ApiResponse.onSuccess(todoService.getTodosByDate(uid, date, tagId));
    }
}