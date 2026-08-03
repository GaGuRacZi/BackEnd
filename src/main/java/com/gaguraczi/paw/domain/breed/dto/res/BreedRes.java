package com.gaguraczi.paw.domain.breed.dto.res;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.users.enums.PetType;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class BreedRes {

    private final Long breedId;
    private final PetType petType;
    private final String name;
    private final boolean popular;

    public static BreedRes from(Breed breed) {
        return BreedRes.builder()
                .breedId(breed.getBreedId())
                .petType(breed.getPetType())
                .name(breed.getName())
                .popular(breed.isPopular())
                .build();
    }
}
