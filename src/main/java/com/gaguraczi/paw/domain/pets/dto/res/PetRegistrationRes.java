package com.gaguraczi.paw.domain.pets.dto.res;

import com.gaguraczi.paw.domain.pets.entity.PetRegistration;

public record PetRegistrationRes(
        Long petId,
        String guardianName,
        String registrationNumber,
        String photoUrl
) {
    public static PetRegistrationRes from(PetRegistration registration) {
        return new PetRegistrationRes(
                registration.getPet().getPetId(),
                registration.getGuardianName(),
                registration.getRegistrationNumber(),
                registration.getPhotoUrl()
        );
    }
}
