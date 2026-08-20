package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoDateRepository extends JpaRepository<TodoDateEntity, Long> {

    Optional<TodoDateEntity> findByTodo_TodoIdAndDate(Long todoId, LocalDate date);

    @EntityGraph(attributePaths = {"todo", "todo.tag"})
    List<TodoDateEntity> findAllByTodo_User_UidAndDate(UUID uid, LocalDate date);

    @EntityGraph(attributePaths = {"todo", "todo.tag"})
    List<TodoDateEntity> findAllByTodo_User_UidAndDateAndTodo_Tag_TagId(UUID uid, LocalDate date, Long tagId);

    List<TodoDateEntity> findAllByTodo_User_UidAndDateBetween(UUID uid, LocalDate start, LocalDate end);

    List<TodoDateEntity> findAllByTodo_TodoIdAndDateBetween(Long todoId, LocalDate start, LocalDate end);

    void deleteAllByTodo_TodoIdAndDateGreaterThanEqualAndCompletedFalse(Long todoId, LocalDate from);

    void deleteAllByTodo_TodoId(Long todoId);

    List<TodoDateEntity> findAllByTodo_TodoIdOrderByDateAsc(Long todoId);

    @Query("""
            SELECT td FROM TodoDateEntity td
            JOIN FETCH td.todo t
            JOIN FETCH t.user
            WHERE td.remindAt >= :from
              AND td.remindAt < :to
              AND td.completed = false
              AND td.notifiedAt IS NULL
              AND td.todoDateId > :afterId
            ORDER BY td.todoDateId
            """)
    List<TodoDateEntity> findDueReminders(
            @Param("from") Instant from,
            @Param("to") Instant to,
            @Param("afterId") Long afterId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE TodoDateEntity td
            SET td.notifiedAt = :now
            WHERE td.todoDateId = :id
              AND td.notifiedAt IS NULL
              AND td.completed = false
            """)
    int markNotifiedIfPending(@Param("id") Long id, @Param("now") Instant now);
}
