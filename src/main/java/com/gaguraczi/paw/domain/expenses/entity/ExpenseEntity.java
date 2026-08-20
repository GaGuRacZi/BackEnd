package com.gaguraczi.paw.domain.expenses.entity;

import com.gaguraczi.paw.domain.expenses.enums.PaymentTypeEnum;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expense")
public class ExpenseEntity extends BaseEntity { // 지출비용(의료비)

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_id")
    private Long expenseId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;


    @Column(name = "expense_name", length = 255, nullable = false)
    private String expenseName;


    @Column(name = "expense_amount", nullable = false)
    private Long expenseAmount;


    @Column(name = "expense_date", nullable = false)
    private LocalDateTime expenseDate;


    @Enumerated(EnumType.STRING)
    @Column(name = "expense_type", nullable = false)
    private PaymentTypeEnum paymentType;


    @Column(name = "expense_address", length = 255)
    private String expenseAddress;

    @Builder.Default
    @OneToMany(mappedBy = "expense", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseDetailEntity> expenseDetails = new ArrayList<>();


    public void addExpenseDetail(ExpenseDetailEntity expenseDetail) {
        this.expenseDetails.add(expenseDetail);
        expenseDetail.linkExpense(this);
    }

    public void replaceExpenseDetails(List<ExpenseDetailEntity> nextDetails) {
        this.expenseDetails.clear();
        for (ExpenseDetailEntity detail : nextDetails) {
            addExpenseDetail(detail);
        }
    }

    public void update(
            String expenseName,
            Long expenseAmount,
            LocalDateTime expenseDate,
            PaymentTypeEnum paymentType,
            String expenseAddress
    ) {
        if (expenseName != null) {
            this.expenseName = expenseName;
        }
        if (expenseAmount != null) {
            this.expenseAmount = expenseAmount;
        }
        if (expenseDate != null) {
            this.expenseDate = expenseDate;
        }
        if (paymentType != null) {
            this.paymentType = paymentType;
        }
        if (expenseAddress != null) {
            this.expenseAddress = expenseAddress;
        }
    }
}
