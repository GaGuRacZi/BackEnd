package com.gaguraczi.paw.domain.expenses.repository;

import com.gaguraczi.paw.domain.expenses.entity.ExpenseDetailEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExpenseDetailRepository extends JpaRepository<ExpenseDetailEntity, Long> {
}
