package com.gaguraczi.paw.domain.expenses.service;

import com.gaguraczi.paw.domain.expenses.dto.request.ExpenseCreateRequest;
import com.gaguraczi.paw.domain.expenses.dto.request.ExpenseDetailCreateRequest;
import com.gaguraczi.paw.domain.expenses.dto.request.ExpenseUpdateRequest;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseDetailResponse;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseItemResponse;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseMonthlyResponse;
import com.gaguraczi.paw.domain.expenses.dto.response.ExpenseSummaryResponse;
import com.gaguraczi.paw.domain.expenses.entity.ExpenseDetailEntity;
import com.gaguraczi.paw.domain.expenses.entity.ExpenseEntity;
import com.gaguraczi.paw.domain.expenses.exception.code.ExpenseErrorCode;
import com.gaguraczi.paw.domain.expenses.repository.ExpenseRepository;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final PetRepository petRepository;
    private final SecurityUtils securityUtils;


    @Transactional
    public ExpenseDetailResponse createExpense(Long petId, ExpenseCreateRequest request) {
        Pet pet = loadOwnedPet(petId);
        LocalDateTime expenseDate = request.expenseDate().atStartOfDay();
        validateNotFuture(expenseDate);
        validateAmountMatchesDetails(request.expenseAmount(), request.expenseDetails());

        ExpenseEntity expense = ExpenseEntity.builder()
                .pet(pet)
                .expenseName(request.expenseName().trim())
                .expenseAmount(request.expenseAmount())
                .expenseDate(expenseDate)
                .paymentType(request.paymentType())
                .build();

        for (ExpenseDetailCreateRequest detailRequest : request.expenseDetails()) {
            expense.addExpenseDetail(toDetailEntity(detailRequest));
        }

        return ExpenseDetailResponse.from(expenseRepository.save(expense));
    }

    @Transactional
    public ExpenseDetailResponse updateExpense(Long expenseId, ExpenseUpdateRequest request) {
        ExpenseEntity expense = loadOwnedExpense(expenseId);

        LocalDateTime expenseDate = request.expenseDate() != null ? request.expenseDate().atStartOfDay() : null;
        validateNotFuture(expenseDate != null ? expenseDate : expense.getExpenseDate());

        expense.update(
                request.expenseName() != null ? request.expenseName().trim() : null,
                request.expenseAmount(),
                expenseDate,
                request.paymentType()
        );

        if (request.expenseDetails() != null) {
            if (request.expenseDetails().isEmpty()) {
                throw GeneralException.of(ExpenseErrorCode.EXPENSE_DETAIL_REQUIRED);
            }
            List<ExpenseDetailEntity> nextDetails = new ArrayList<>();
            for (ExpenseDetailCreateRequest detailRequest : request.expenseDetails()) {
                nextDetails.add(toDetailEntity(detailRequest));
            }
            expense.replaceExpenseDetails(nextDetails);
        }

        return ExpenseDetailResponse.from(expense);
    }

    @Transactional
    public void deleteExpense(Long expenseId) {
        ExpenseEntity expense = loadOwnedExpense(expenseId);
        expenseRepository.delete(expense);
    }


    public ExpenseMonthlyResponse getMonthlyExpenses(Long petId, Integer year, Integer month) {
        loadOwnedPet(petId);
        YearMonth yearMonth = resolveYearMonth(year, month);

        List<ExpenseItemResponse> expenses = findExpensesOfMonth(petId, yearMonth).stream()
                .map(ExpenseItemResponse::from)
                .toList();

        return ExpenseMonthlyResponse.of(yearMonth.getYear(), yearMonth.getMonthValue(), expenses);
    }


    public ExpenseSummaryResponse getExpenseSummary(Long petId, Integer year, Integer month) {
        loadOwnedPet(petId);
        YearMonth yearMonth = resolveYearMonth(year, month);

        LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        // 이번 달 병원비는 기록별 결제금액(expenseAmount) 합계 기준
        Long monthlyTotalAmount = expenseRepository.sumExpenseAmountByPetIdAndPeriod(
                petId, startDateTime, endDateTime);

        Long totalAmount = expenseRepository.sumExpenseAmountByPetId(petId);

        return new ExpenseSummaryResponse(
                yearMonth.getYear(),
                yearMonth.getMonthValue(),
                monthlyTotalAmount == null ? 0L : monthlyTotalAmount,
                totalAmount == null ? 0L : totalAmount
        );
    }


    public ExpenseDetailResponse getExpense(Long expenseId) {
        return ExpenseDetailResponse.from(loadOwnedExpense(expenseId));
    }

    private List<ExpenseEntity> findExpensesOfMonth(Long petId, YearMonth yearMonth) {
        LocalDateTime startDateTime = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDateTime = yearMonth.plusMonths(1).atDay(1).atStartOfDay();

        return expenseRepository.findAllWithDetailsByPetIdAndPeriod(petId, startDateTime, endDateTime);
    }

    private YearMonth resolveYearMonth(Integer year, Integer month) {
        if (year == null && month == null) {
            return YearMonth.now();
        }
        if (year == null || month == null || month < 1 || month > 12) {
            throw GeneralException.of(ExpenseErrorCode.EXPENSE_INVALID_PERIOD);
        }
        return YearMonth.of(year, month);
    }

    private void validateAmountMatchesDetails(Long expenseAmount, List<ExpenseDetailCreateRequest> details) {
        long detailsSum = details.stream()
                .mapToLong(detail -> detail.expenseAmount().longValue())
                .sum();
        if (detailsSum != expenseAmount) {
            throw GeneralException.of(ExpenseErrorCode.EXPENSE_AMOUNT_MISMATCH);
        }
    }

    private void validateNotFuture(LocalDateTime expenseDate) {
        if (expenseDate != null && expenseDate.toLocalDate().isAfter(LocalDate.now())) {
            throw GeneralException.of(ExpenseErrorCode.EXPENSE_FUTURE_NOT_ALLOWED);
        }
    }

    private ExpenseDetailEntity toDetailEntity(ExpenseDetailCreateRequest detailRequest) {
        return ExpenseDetailEntity.builder()
                .expenseDetailName(detailRequest.expenseDetailName().trim())
                .expenseAmount(detailRequest.expenseAmount())
                .build();
    }

    private Pet loadOwnedPet(Long petId) {
        User user = securityUtils.currentUser();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(user.getUid())) {
            throw GeneralException.of(PetErrorCode.PET_NOT_FOUND);
        }
        return pet;
    }

    private ExpenseEntity loadOwnedExpense(Long expenseId) {
        UUID uid = securityUtils.currentUid();
        ExpenseEntity expense = expenseRepository.findWithDetailsByExpenseId(expenseId)
                .orElseThrow(() -> GeneralException.of(ExpenseErrorCode.EXPENSE_NOT_FOUND));
        if (!expense.getPet().getUser().getUid().equals(uid)) {
            throw GeneralException.of(ExpenseErrorCode.EXPENSE_FORBIDDEN);
        }
        return expense;
    }
}
