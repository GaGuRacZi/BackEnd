package com.gaguraczi.paw.domain.weights.repository;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.weights.entity.PetWeightEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface PetWeightRepository extends JpaRepository<PetWeightEntity, Long> {


    List<PetWeightEntity> findAllByPetAndRecordedAtBetweenOrderByRecordedAtAsc(
            Pet pet, LocalDateTime start, LocalDateTime end);


    Optional<PetWeightEntity> findFirstByPetOrderByRecordedAtDescPetWeightIdDesc(Pet pet);


    Optional<PetWeightEntity> findFirstByPetAndRecordedAtLessThanOrderByRecordedAtDescPetWeightIdDesc(
            Pet pet, LocalDateTime before);


    List<PetWeightEntity> findTop2ByPetOrderByRecordedAtDescPetWeightIdDesc(Pet pet);


    List<PetWeightEntity> findAllByPetOrderByRecordedAtDescPetWeightIdDesc(Pet pet);

    Optional<PetWeightEntity> findByPetWeightIdAndPet(Long petWeightId, Pet pet);
}
