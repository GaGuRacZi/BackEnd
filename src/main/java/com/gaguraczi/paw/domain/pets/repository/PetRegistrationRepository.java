package com.gaguraczi.paw.domain.pets.repository;

import com.gaguraczi.paw.domain.pets.entity.PetRegistration;
import com.gaguraczi.paw.domain.users.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PetRegistrationRepository extends JpaRepository<PetRegistration, Long> {

    Optional<PetRegistration> findByPet(Pet pet);
}
