package com.gaguraczi.paw.domain.pets.dto.res;

import com.gaguraczi.paw.domain.pets.entity.PetCareCode;
import com.gaguraczi.paw.domain.users.enums.PetType;

public record PetCareCodeRes(
        Long codeId,
        String name,
        PetType species
) {
    public static PetCareCodeRes from(PetCareCode code) {
        return new PetCareCodeRes(code.getPetCareCodeId(), code.getName(), code.getSpecies());
    }
}
