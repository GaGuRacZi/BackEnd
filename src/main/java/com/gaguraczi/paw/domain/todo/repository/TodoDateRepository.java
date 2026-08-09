package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TodoDateEntity;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

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
}