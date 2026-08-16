package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.pets.dto.req.PetRegistrationReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRegistrationRes;
import com.gaguraczi.paw.domain.pets.entity.PetRegistration;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.pets.repository.PetRegistrationRepository;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetRegistrationService {

    private static final String DIRECTORY = "pet-registration";

    private final PetRegistrationRepository petRegistrationRepository;
    private final PetRepository petRepository;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    public PetRegistrationRes get(Long petId) {
        Pet pet = findOwnedPet(petId);
        PetRegistration registration = petRegistrationRepository.findByPet(pet)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_REGISTRATION_NOT_FOUND));
        return PetRegistrationRes.from(registration);
    }

    @Transactional
    public PetRegistrationRes upsert(Long petId, PetRegistrationReq req, MultipartFile photo) {
        Pet pet = findOwnedPet(petId);
        PetRegistration registration = petRegistrationRepository.findByPet(pet).orElse(null);

        if (registration == null) {
            registration = PetRegistration.builder()
                    .pet(pet)
                    .guardianName(req.guardianName().trim())
                    .registrationNumber(req.registrationNumber().trim())
                    .build();
            petRegistrationRepository.save(registration);
        } else {
            registration.update(req.guardianName(), req.registrationNumber());
        }

        if (photo != null) {
            if (photo.isEmpty()) {
                throw GeneralException.of(PetErrorCode.PET_IMAGE_EMPTY);
            }
            String previousKey = registration.getPhotoS3Key();
            PetRegistration finalRegistration = registration;
            s3Utils.replaceUnderDirectory(
                    photo,
                    DIRECTORY,
                    previousKey,
                    uploaded -> finalRegistration.updatePhoto(uploaded.getKey(), uploaded.getUrl())
            );
        }

        return PetRegistrationRes.from(registration);
    }

    private Pet findOwnedPet(Long petId) {
        User user = securityUtils.currentUser();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(user.getUid())) {
            throw GeneralException.of(PetErrorCode.PET_NOT_FOUND);
        }
        return pet;
    }
}
