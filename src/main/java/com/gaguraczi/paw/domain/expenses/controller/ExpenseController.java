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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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

@Tag(name = "Expense", description = ExpenseApiDocs.TAG_DESCRIPTION)
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ExpenseController {

    private final ExpenseService expenseService;

    @Operation(
            summary = "의료비 기록하기",
            description = ExpenseApiDocs.CREATE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(name = "의료비 등록", value = ExpenseApiDocs.CREATE_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_CREATE_200",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_CREATE_200", value = ExpenseApiDocs.DETAIL_RESULT_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "EXPENSE_400_3 미래 날짜 / EXPENSE_400_4 결제금액≠세부합계 / EXPENSE_400_1 세부항목 없음",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "EXPENSE_400_4", value = ExpenseApiDocs.EXPENSE_400_4_EXAMPLE),
                            @ExampleObject(name = "EXPENSE_400_3", value = ExpenseApiDocs.EXPENSE_400_3_EXAMPLE),
                            @ExampleObject(name = "EXPENSE_400_1", value = ExpenseApiDocs.EXPENSE_400_1_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404. 없거나 본인 펫이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_404", value = ExpenseApiDocs.PET_404_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "401",
                    description = ExpenseApiDocs.JWT_401_1_DESCRIPTION,
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "JWT_401_1", value = ExpenseApiDocs.JWT_401_1_EXAMPLE))
            )
    })
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

    @Operation(
            summary = "의료비 기록 수정",
            description = ExpenseApiDocs.UPDATE_DESCRIPTION,
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(
                            examples = @ExampleObject(name = "세부항목 교체", value = ExpenseApiDocs.UPDATE_REQ_EXAMPLE)
                    )
            )
    )
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_UPDATE_200. expenseDetails를 보냈으면 id가 새로 발급됩니다.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_UPDATE_200", value = ExpenseApiDocs.UPDATE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "EXPENSE_400_1 빈 세부항목 / EXPENSE_400_3 미래 날짜",
                    content = @Content(mediaType = "application/json", examples = {
                            @ExampleObject(name = "EXPENSE_400_1", value = ExpenseApiDocs.EXPENSE_400_1_EXAMPLE),
                            @ExampleObject(name = "EXPENSE_400_3", value = ExpenseApiDocs.EXPENSE_400_3_EXAMPLE)
                    })
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "EXPENSE_403. 본인 기록이 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_403", value = ExpenseApiDocs.EXPENSE_403_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "EXPENSE_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_404", value = ExpenseApiDocs.EXPENSE_404_EXAMPLE))
            )
    })
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

    @Operation(summary = "의료비 기록 삭제", description = ExpenseApiDocs.DELETE_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_DELETE_200. result=null.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_DELETE_200", value = ExpenseApiDocs.DELETE_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "EXPENSE_403",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_403", value = ExpenseApiDocs.EXPENSE_403_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "EXPENSE_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_404", value = ExpenseApiDocs.EXPENSE_404_EXAMPLE))
            )
    })
    @DeleteMapping("/expenses/{expenseId}")
    public ApiResponse<Void> deleteExpense(
            @Parameter(description = "의료비 id", example = "1") @PathVariable Long expenseId
    ) {
        expenseService.deleteExpense(expenseId);
        return ApiResponse.onSuccess(ExpenseSuccessCode.EXPENSE_DELETE_200, null);
    }

    @Operation(summary = "월별 의료비 내역 목록", description = ExpenseApiDocs.LIST_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_LIST_200. 최신 이용일 순.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_LIST_200", value = ExpenseApiDocs.LIST_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "EXPENSE_400_2. year/month 한쪽만 보냈거나 month가 1~12가 아님.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_400_2", value = ExpenseApiDocs.EXPENSE_400_2_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "PET_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "PET_404", value = ExpenseApiDocs.PET_404_EXAMPLE))
            )
    })
    @GetMapping("/pets/{petId}/expenses")
    public ApiResponse<ExpenseMonthlyResponse> getMonthlyExpenses(
            @Parameter(description = "반려동물 id", example = "1") @PathVariable Long petId,
            @Parameter(description = "조회 연도. month와 함께 지정. 둘 다 생략 시 이번 달", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "조회 월(1~12). year와 함께 지정", example = "7")
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_LIST_200,
                expenseService.getMonthlyExpenses(petId, year, month)
        );
    }

    @Operation(summary = "의료비 요약", description = ExpenseApiDocs.SUMMARY_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_SUMMARY_200. 상단 카드.",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_SUMMARY_200", value = ExpenseApiDocs.SUMMARY_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "400",
                    description = "EXPENSE_400_2",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_400_2", value = ExpenseApiDocs.EXPENSE_400_2_EXAMPLE))
            )
    })
    @GetMapping("/pets/{petId}/expenses/summary")
    public ApiResponse<ExpenseSummaryResponse> getExpenseSummary(
            @Parameter(description = "반려동물 id", example = "1") @PathVariable Long petId,
            @Parameter(description = "조회 연도. month와 함께 지정. 둘 다 생략 시 이번 달", example = "2026")
            @RequestParam(required = false) Integer year,
            @Parameter(description = "조회 월(1~12). year와 함께 지정", example = "7")
            @RequestParam(required = false) Integer month
    ) {
        return ApiResponse.onSuccess(
                ExpenseSuccessCode.EXPENSE_SUMMARY_200,
                expenseService.getExpenseSummary(petId, year, month)
        );
    }

    @Operation(summary = "의료비 상세 조회", description = ExpenseApiDocs.DETAIL_DESCRIPTION)
    @ApiResponses({
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "200",
                    description = "EXPENSE_DETAIL_200",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_DETAIL_200", value = ExpenseApiDocs.DETAIL_200_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "403",
                    description = "EXPENSE_403",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_403", value = ExpenseApiDocs.EXPENSE_403_EXAMPLE))
            ),
            @io.swagger.v3.oas.annotations.responses.ApiResponse(
                    responseCode = "404",
                    description = "EXPENSE_404",
                    content = @Content(mediaType = "application/json", examples = @ExampleObject(
                            name = "EXPENSE_404", value = ExpenseApiDocs.EXPENSE_404_EXAMPLE))
            )
    })
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
