package com.gaguraczi.paw.domain.todo.controller;

import com.gaguraczi.paw.domain.todo.dto.request.TodoCreateRequest;
import com.gaguraczi.paw.domain.todo.dto.request.TodoUpdateRequest;
import com.gaguraczi.paw.domain.todo.dto.response.TodoDetailResponse;
import com.gaguraczi.paw.domain.todo.dto.response.TodoListResponse;
import com.gaguraczi.paw.domain.todo.exception.code.TodoSuccessCode;
import com.gaguraczi.paw.domain.todo.service.TodoService;
import com.gaguraczi.paw.global.api.ApiResponse;
import com.gaguraczi.paw.global.security.SecurityUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
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
    private final SecurityUtils securityUtils;

    @GetMapping
    @Operation(summary = "날짜별 투두 목록 조회",
            description = "선택한 날짜의 투두 목록을 조회합니다. tagId를 주면 해당 태그만 필터링합니다.")
    public ApiResponse<List<TodoListResponse>> getTodosByDate(
            @Parameter(description = "조회 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "태그 필터") @RequestParam(required = false) Long tagId
    ) {
        UUID uid = securityUtils.currentUid();
        return ApiResponse.onSuccess(
                TodoSuccessCode.TODO_LIST_200,
                todoService.getTodosByDate(uid, date, tagId)
        );
    }

    @GetMapping("/{todoId}")
    @Operation(summary = "투두 상세 조회",
            description = "투두 단건의 상세 정보를 조회합니다.")
    public ApiResponse<TodoDetailResponse> getTodoDetail(
            @Parameter(description = "투두 ID") @PathVariable Long todoId
    ) {
        UUID uid = securityUtils.currentUid();
        return ApiResponse.onSuccess(
                TodoSuccessCode.TODO_GET_200,
                todoService.getTodoDetail(uid, todoId)
        );
    }

    @PostMapping
    @Operation(summary = "투두 생성",
            description = "투두를 생성합니다. routineEnabled=false면 date가 필수이고, "
                    + "routineEnabled=true면 endDate와 week가 필수입니다.")
    public ApiResponse<TodoDetailResponse> createTodo(
            @Valid @RequestBody TodoCreateRequest request
    ) {
        UUID uid = securityUtils.currentUid();
        return ApiResponse.onSuccess(
                TodoSuccessCode.TODO_CREATE_201,
                todoService.createTodo(uid, request)
        );
    }

    @PutMapping("/{todoId}")
    @Operation(summary = "투두 수정",
            description = "투두 내용을 수정합니다. 루틴 투두는 오늘 이후의 미완료 일정만 다시 생성됩니다.")
    public ApiResponse<TodoDetailResponse> updateTodo(
            @Parameter(description = "투두 ID") @PathVariable Long todoId,
            @Valid @RequestBody TodoUpdateRequest request
    ) {
        UUID uid = securityUtils.currentUid();
        return ApiResponse.onSuccess(
                TodoSuccessCode.TODO_UPDATE_200,
                todoService.updateTodo(uid, todoId, request)
        );
    }

    @PatchMapping("/{todoId}/complete")
    @Operation(summary = "투두 완료 처리",
            description = "해당 날짜의 투두 완료 상태를 변경합니다.")
    public ApiResponse<TodoListResponse> updateComplete(
            @Parameter(description = "투두 ID") @PathVariable Long todoId,
            @Parameter(description = "대상 날짜 (yyyy-MM-dd)")
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "완료 여부") @RequestParam boolean completed
    ) {
        UUID uid = securityUtils.currentUid();
        return ApiResponse.onSuccess(
                TodoSuccessCode.TODO_COMPLETE_200,
                todoService.updateComplete(uid, todoId, date, completed)
        );
    }

    @DeleteMapping("/{todoId}")
    @Operation(summary = "투두 삭제",
            description = "투두를 삭제합니다. 루틴 투두는 deleteAll=false일 때 date에 해당하는 하루만 삭제하고, "
                    + "deleteAll=true면 전체를 삭제합니다.")
    public ApiResponse<Void> deleteTodo(
            @Parameter(description = "투두 ID") @PathVariable Long todoId,
            @Parameter(description = "삭제할 날짜 (yyyy-MM-dd), 루틴 투두 하루만 삭제할 때 필수")
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @Parameter(description = "루틴 전체 삭제 여부") @RequestParam(defaultValue = "false") boolean deleteAll
    ) {
        UUID uid = securityUtils.currentUid();
        todoService.deleteTodo(uid, todoId, date, deleteAll);
        return ApiResponse.onSuccess(TodoSuccessCode.TODO_DELETE_200, null);
    }

}
