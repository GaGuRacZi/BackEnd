package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import com.gaguraczi.paw.domain.todo.enums.WeekEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TodoRepository extends JpaRepository<TodoEntity, Long> {

    @Query("select t from TodoEntity t join fetch t.tag where t.todoId = :todoId and t.user.uid = :uid")
    Optional<TodoEntity> findMyTodo(@Param("todoId") Long todoId, @Param("uid") UUID uid);

    List<TodoEntity> findAllByRoutineEnabledTrue();

    boolean existsByTag_TagId(Long tagId);

    List<TodoEntity> findAllByUser_UidAndTag_TagId(UUID uid, Long tagId);

    @Query("""
            SELECT t FROM TodoEntity t
            JOIN FETCH t.user
            WHERE t.routineEnabled = true
              AND t.week = :week
              AND t.startDate <= :today
              AND t.endDate >= :today
              AND t.todoTime >= :fromTime
              AND (:wrapsMidnight = true OR t.todoTime < :toTime)
            """)
    List<TodoEntity> findRoutinesDueThisMinute(
            @Param("week") WeekEnum week,
            @Param("today") LocalDate today,
            @Param("fromTime") LocalTime fromTime,
            @Param("toTime") LocalTime toTime,
            @Param("wrapsMidnight") boolean wrapsMidnight
    );
}
