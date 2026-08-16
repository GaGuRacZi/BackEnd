package com.gaguraczi.paw.domain.pets.repository;

import com.gaguraczi.paw.domain.pets.entity.PetCareSelection;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.users.entity.Pet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PetCareSelectionRepository extends JpaRepository<PetCareSelection, Long> {

    List<PetCareSelection> findByPetAndCode_Category(Pet pet, PetCareCategory category);

    void deleteByPetAndCode_Category(Pet pet, PetCareCategory category);
}
