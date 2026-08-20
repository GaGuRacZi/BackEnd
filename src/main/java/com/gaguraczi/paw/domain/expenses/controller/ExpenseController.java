package com.gaguraczi.paw.domain.expenses.controller;

import com.gaguraczi.paw.domain.expenses.dto.request.ExpenseCreateRequest;
import com.gaguraczi.paw.domain.expenses.dto.request.ExpenseUpdateRequest;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseDetailResponse;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseMonthlyResponse;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseSummaryResponse;
import com.gaguraczi.paw.domain.expenses.exception.code.ExpenseSuccessCode;
import com.gaguraczi.paw.domain.expenses.service.ExpenseService;
import com.gaguraczi.paw.global.api.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Expense", description = "건강요약 - 의료비")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(summary = "의료비 기록하기", description = "본인 소유 펫만 기록할 수 있습니다. 결제금액/이용날짜/결제수단/병원명,세부 항목을 함께 저장합니다. 미래 날짜는 기록할 수 없습니다.")
    @PostMapping("/pets/{petId}/expenses")
    public ApiResponse<ExpenseDetailResponse> createExpense(
            @Parameter(description = "반려동물 id", example = "1") @PathVariable Long petId,
            @Valid @RequestBody ExpenseCreateRequest request
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_CREATE_200,
                expenseService.createExpense(petId, request)
        );
    }

    @Operation(summary = "의료비 기록 수정", description = "작성자 본인만 가능합니다. 보낸 필드만 반영됩니다. expenseDetails를 보내면 기존 세부 항목 전체가 교체됩니다.")
    @PutMapping("/expenses/{expenseId}")
    public ApiResponse<ExpenseDetailResponse> updateExpense(
            @Parameter(description = "의료비 id", example = "1") @PathVariable Long expenseId,
            @Valid @RequestBody ExpenseUpdateRequest request
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_UPDATE_200,
                expenseService.updateExpense(expenseId, request)
        );
    }

    @Operation(summary = "의료비 기록 삭제", description = "작성자 본인만 가능합니다.")
    @DeleteMapping("/expenses/{expenseId}")
    public ApiResponse<Void> deleteExpense(
            @Parameter(description = "의료비 id", example = "1") @PathVariable Long expenseId
    ) {
        expenseService.deleteExpense(expenseId);
        return ApiResponse.onSuccess(ExpenseSuccessCode.EXPENSE_DELETE_200, null);
    }

    @Operation(summary = "월별 의료비 내역 목록", description = "지정한 연월의 의료비 내역을 최신순으로 조회합니다. 연월 미지정 시 이번 달을 조회합니다.")
    @GetMapping("/pets/{petId}/expenses")
    public ApiResponse<ExpenseMonthlyResponse> getMonthlyExpenses(
            @Parameter(description = "반려동물 id", example = "1") @PathVariable Long petId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam(required = false) Integer year,
            @Parameter(description = "조회 월", example = "7") @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_LIST_200,
                expenseService.getMonthlyExpenses(petId, year, month)
        );
    }

    @Operation(summary = "의료비 요약", description = "이번 달 병원비, 누적 총 병원비를 조회합니다.")
    @GetMapping("/pets/{petId}/expenses/summary")
    public ApiResponse<ExpenseSummaryResponse> getExpenseSummary(
            @Parameter(description = "반려동물 id", example = "1") @PathVariable Long petId,
            @Parameter(description = "조회 연도", example = "2026") @RequestParam(required = false) Integer year,
            @Parameter(description = "조회 월", example = "7") @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_SUMMARY_200,
                expenseService.getExpenseSummary(petId, year, month)
        );
    }

    @Operation(summary = "의료비 상세 조회", description = "작성자 본인만 조회할 수 있습니다. 의료비 단건과 세부 항목 목록을 조회합니다.")
    @GetMapping("/expenses/{expenseId}")
    public ApiResponse<ExpenseDetailResponse> getExpense(
            @Parameter(description = "의료비 id", example = "1") @PathVariable Long expenseId
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_DETAIL_200,
                expenseService.getExpense(expenseId)
        );
    }
}
