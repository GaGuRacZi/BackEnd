package com.gaguraczi.paw.domain.users.repository;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PetRepository extends JpaRepository<Pet, Long> {

    List<Pet> findByUser(User user);

    boolean existsByUser(User user);

    Optional<Pet> findFirstByUserAndIsMainTrue(User user);
}
