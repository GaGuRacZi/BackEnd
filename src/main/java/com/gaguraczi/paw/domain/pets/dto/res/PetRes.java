package com.gaguraczi.paw.domain.pets.dto.res;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Builder
public class PetRes {

    private final Long petId;
    private final PetType petType;
    private final Long breedId;
    private final String breedName;
    private final String petName;
    private final LocalDate birth;
    private final BigDecimal petWeight;
    private final Gender gender;
    private final Boolean neutering;
    private final boolean main;
    private final String profileUrl;

    public static PetRes from(Pet pet) {
        return PetRes.builder()
                .petId(pet.getPetId())
                .petType(pet.getPetType())
                .breedId(pet.getBreed() != null ? pet.getBreed().getBreedId() : null)
                .breedName(pet.getBreedName())
                .petName(pet.getPetName())
                .birth(pet.getBirth())
                .petWeight(pet.getPetWeight())
                .gender(pet.getGender())
                .neutering(pet.getNeutering())
                .main(pet.isMain())
                .profileUrl(pet.getProfileUrl())
                .build();
    }
}
