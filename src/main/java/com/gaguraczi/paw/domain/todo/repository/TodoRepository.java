package com.gaguraczi.paw.domain.todo.repository;

import com.gaguraczi.paw.domain.todo.entity.TodoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

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
}