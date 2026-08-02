package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.breed.service.BreedService;
import com.gaguraczi.paw.domain.pets.dto.req.PetCreateReq;
import com.gaguraczi.paw.domain.pets.dto.req.PetUpdateReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRes;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final BreedService breedService;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    @Transactional
    public PetRes create(PetCreateReq req, MultipartFile image) {
        User user = securityUtils.currentUser();

        Breed breed = null;
        String breedName = req.getBreed();
        if (req.getBreedId() != null) {
            breed = breedService.requireBreed(req.getBreedId(), req.getPetType());
            breedName = breed.getName();
        }
        if ((breedName == null || breedName.isBlank()) && breed == null) {
            throw GeneralException.of(PetErrorCode.PET_BREED_REQUIRED);
        }

        S3Dto uploaded = uploadProfileImage(image);

        try {
            boolean isFirstPet = petRepository.findByUser(user).isEmpty();

            Pet pet = Pet.builder()
                    .user(user)
                    .petType(req.getPetType())
                    .breed(breed)
                    .breedName(breedName == null ? null : breedName.trim())
                    .petName(req.getPetName().trim())
                    .birth(req.getBirth())
                    .petWeight(req.getPetWeight())
                    .gender(req.getGender())
                    .neutering(req.getNeutering())
                    .profileS3Key(uploaded != null ? uploaded.getKey() : null)
                    .profileUrl(uploaded != null ? uploaded.getUrl() : null)
                    .isMain(isFirstPet)
                    .build();
            petRepository.save(pet);
            return PetRes.from(pet);
        } catch (RuntimeException e) {
            deleteQuietly(uploaded);
            throw e;
        }
    }

    @Transactional
    public PetRes update(Long petId, PetUpdateReq req, MultipartFile image) {
        User user = securityUtils.currentUser();
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(user.getUid())) {
            throw GeneralException.of(PetErrorCode.PET_NOT_FOUND);
        }

        if (req != null) {
            applyUpdate(pet, req);
        }

        if (image != null) {
            if (image.isEmpty()) {
                throw GeneralException.of(PetErrorCode.PET_IMAGE_EMPTY);
            }
            S3Dto uploaded = s3Utils.uploadMultipartUnderDirectory(image, "pet");
            String previousKey = pet.getProfileS3Key();
            try {
                pet.updateProfileImage(uploaded.getKey(), uploaded.getUrl());
            } catch (RuntimeException e) {
                deleteQuietly(uploaded);
                throw e;
            }
            deleteQuietly(previousKey);
        }

        return PetRes.from(pet);
    }

    private void applyUpdate(Pet pet, PetUpdateReq req) {
        PetType petType = req.getPetType() != null ? req.getPetType() : pet.getPetType();
        Breed breed = pet.getBreed();
        String breedName = pet.getBreedName();

        boolean breedTouched = req.getBreedId() != null || req.getBreed() != null;
        if (breedTouched) {
            breed = null;
            breedName = req.getBreed();
            if (req.getBreedId() != null) {
                breed = breedService.requireBreed(req.getBreedId(), petType);
                breedName = breed.getName();
            }
            if ((breedName == null || breedName.isBlank()) && breed == null) {
                throw GeneralException.of(PetErrorCode.PET_BREED_REQUIRED);
            }
            breedName = breedName == null ? null : breedName.trim();
        }

        pet.update(
                req.getPetType(),
                breedTouched ? breed : null,
                breedTouched ? breedName : null,
                blankToNull(req.getPetName()),
                req.getBirth(),
                req.getPetWeight(),
                req.getGender(),
                req.getNeutering()
        );
    }

    private S3Dto uploadProfileImage(MultipartFile image) {
        if (image == null) {
            return null;
        }
        if (image.isEmpty()) {
            throw GeneralException.of(PetErrorCode.PET_IMAGE_EMPTY);
        }
        return s3Utils.uploadMultipartUnderDirectory(image, "pet");
    }

    private void deleteQuietly(S3Dto uploaded) {
        if (uploaded == null) {
            return;
        }
        deleteQuietly(uploaded.getKey());
    }

    private void deleteQuietly(String key) {
        if (key == null || key.isBlank()) {
            return;
        }
        try {
            s3Utils.deleteFile(key);
        } catch (Exception ex) {
            log.warn("Failed to cleanup S3 object: {}", key, ex);
        }
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
