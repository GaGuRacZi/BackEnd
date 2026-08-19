package com.gaguraczi.paw.domain.users.repository;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByUser(User user);

    boolean existsByUser(User user);

    Optional<Pet> findFirstByUserAndIsMainTrue(User user);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Pet p where p.petId = :petId")
    Optional<Pet> findByIdForUpdate(@Param("petId") Long petId);
}
