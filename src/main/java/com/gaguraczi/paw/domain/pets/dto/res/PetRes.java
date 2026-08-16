package com.gaguraczi.paw.domain.pets.dto.res;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PetRes(
        Long petId,
        PetType petType,
        Long breedId,
        String breedName,
        String petName,
        LocalDate birth,
        BigDecimal petWeight,
        Gender gender,
        Boolean neutering,
        boolean main,
        String profileUrl,
        String bloodType
) {
    public static PetRes from(Pet pet) {
        return new PetRes(
                pet.getPetId(),
                pet.getPetType(),
                pet.getBreed() != null ? pet.getBreed().getBreedId() : null,
                pet.getBreedName(),
                pet.getPetName(),
                pet.getBirth(),
                pet.getPetWeight(),
                pet.getGender(),
                pet.getNeutering(),
                pet.isMain(),
                pet.getProfileUrl(),
                pet.getBloodType()
        );
    }
}
