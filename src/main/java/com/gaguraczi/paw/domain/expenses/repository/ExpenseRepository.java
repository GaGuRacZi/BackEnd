package com.gaguraczi.paw.domain.expenses.repository;

import com.gaguraczi.paw.domain.expenses.entity.ExpenseEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<ExpenseEntity, Long> {


    @Query("""
            select distinct e
            from ExpenseEntity e
            left join fetch e.expenseDetails
            where e.pet.petId = :petId
              and e.expenseDate >= :startDateTime
              and e.expenseDate < :endDateTime
            order by e.expenseDate desc
            """)
    List<ExpenseEntity> findAllWithDetailsByPetIdAndPeriod(
            @Param("petId") Long petId,
            @Param("startDateTime") LocalDateTime startDateTime,
            @Param("endDateTime") LocalDateTime endDateTime
    );


    @Query("""
            select distinct e
            from ExpenseEntity e
            left join fetch e.expenseDetails
            where e.expenseId = :expenseId
            """)
    Optional<ExpenseEntity> findWithDetailsByExpenseId(@Param("expenseId") Long expenseId);


    @Query("""
            select coalesce(sum(e.expenseAmount), 0L)
            from ExpenseEntity e
            where e.pet.petId = :petId
            """)
    Long sumExpenseAmountByPetId(@Param("petId") Long petId);
}
