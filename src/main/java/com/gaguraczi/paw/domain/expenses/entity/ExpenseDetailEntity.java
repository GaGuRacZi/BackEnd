package com.gaguraczi.paw.domain.expenses.entity;

import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "expense_detail")
public class ExpenseDetailEntity extends BaseEntity { // 비용 세부항목

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "expense_detail_id")
    private Long expenseDetailId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    private ExpenseEntity expense;


    @Column(name = "expense_detail_name", length = 255, nullable = false)
    private String expenseDetailName;


    @Column(name = "expense_amount", nullable = false)
    private Integer expenseAmount;


    void linkExpense(ExpenseEntity expense) {
        this.expense = expense;
    }
}
