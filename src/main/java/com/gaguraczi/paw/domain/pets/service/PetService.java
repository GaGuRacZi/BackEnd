package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.breed.service.BreedService;
import com.gaguraczi.paw.domain.pets.dto.req.PetCreateReq;
import com.gaguraczi.paw.domain.pets.dto.req.PetUpdateReq;
import com.gaguraczi.paw.domain.pets.dto.res.PetRes;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.CatBloodType;
import com.gaguraczi.paw.domain.users.enums.DogBloodType;
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

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetService {

    private final PetRepository petRepository;
    private final BreedService breedService;
    private final SecurityUtils securityUtils;
    private final S3Utils s3Utils;

    public List<PetRes> getMyPets() {
        User user = securityUtils.currentUser();
        return petRepository.findByUserOrderByIsMainDescPetIdAsc(user).stream()
                .map(PetRes::from)
                .toList();
    }

    public PetRes getPet(Long petId) {
        Pet pet = findOwnedPet(petId);
        return PetRes.from(pet);
    }

    @Transactional
    public PetRes setMainPet(Long petId) {
        User user = securityUtils.currentUser();
        Pet target = findOwnedPet(petId);
        petRepository.findFirstByUserAndIsMainTrue(user)
                .filter(current -> !current.getPetId().equals(petId))
                .ifPresent(current -> current.setMain(false));
        target.setMain(true);
        return PetRes.from(target);
    }

    @Transactional
    public void delete(Long petId) {
        User user = securityUtils.currentUser();
        Pet pet = findOwnedPet(petId);
        boolean wasMain = pet.isMain();
        String profileS3Key = pet.getProfileS3Key();

        petRepository.delete(pet);

        if (wasMain) {
            petRepository.findFirstByUserAndPetIdNotOrderByCreatedAtDesc(user, petId)
                    .ifPresent(next -> next.setMain(true));
        }
        if (profileS3Key != null) {
            s3Utils.deleteQuietly(profileS3Key);
        }
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

    private String resolveBloodType(PetType petType, String bloodType) {
        if (bloodType == null || bloodType.isBlank()) {
            return null;
        }
        String normalized = bloodType.trim().toUpperCase();
        boolean valid = switch (petType) {
            case DOG -> isValidEnumName(DogBloodType.class, normalized);
            case CAT -> isValidEnumName(CatBloodType.class, normalized);
        };
        if (!valid) {
            throw GeneralException.of(PetErrorCode.PET_BLOOD_TYPE_MISMATCH);
        }
        return normalized;
    }

    private static <E extends Enum<E>> boolean isValidEnumName(Class<E> enumClass, String name) {
        for (E value : enumClass.getEnumConstants()) {
            if (value.name().equals(name)) {
                return true;
            }
        }
        return false;
    }

    @Transactional
    public PetRes create(PetCreateReq req, MultipartFile image) {
        User user = securityUtils.currentUser();

        Breed breed = breedService.resolveBreed(req.breedId(), req.breed(), req.petType());
        String breedName = breed != null ? breed.getName() : blankToNull(req.breed());
        if (breed == null && breedName == null) {
            throw GeneralException.of(PetErrorCode.PET_BREED_REQUIRED);
        }

        String bloodType = resolveBloodType(req.petType(), req.bloodType());
        S3Dto uploaded = uploadProfileImage(image);

        try {
            boolean isFirstPet = !petRepository.existsByUser(user);

            Pet pet = Pet.builder()
                    .user(user)
                    .petType(req.petType())
                    .breed(breed)
                    .breedName(breedName == null ? null : breedName.trim())
                    .petName(req.petName().trim())
                    .birth(req.birth())
                    .petWeight(req.petWeight())
                    .gender(req.gender())
                    .neutering(req.neutering())
                    .bloodType(bloodType == null ? DogBloodType.NONE.name() : bloodType)
                    .profileS3Key(uploaded != null ? uploaded.getKey() : null)
                    .profileUrl(uploaded != null ? uploaded.getUrl() : null)
                    .isMain(isFirstPet)
                    .build();
            petRepository.save(pet);
            return PetRes.from(pet);
        } catch (RuntimeException e) {
            if (uploaded != null) {
                s3Utils.deleteQuietly(uploaded.getKey());
            }
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
            String previousKey = pet.getProfileS3Key();
            s3Utils.replaceUnderDirectory(
                    image,
                    "pet",
                    previousKey,
                    uploaded -> pet.updateProfileImage(uploaded.getKey(), uploaded.getUrl())
            );
        }

        return PetRes.from(pet);
    }

    private void applyUpdate(Pet pet, PetUpdateReq req) {
        PetType petType = req.petType() != null ? req.petType() : pet.getPetType();
        Breed breed = pet.getBreed();
        String breedName = pet.getBreedName();

        boolean breedTouched = req.breedId() != null || req.breed() != null;
        boolean petTypeChanged = req.petType() != null && req.petType() != pet.getPetType();
        boolean hasExistingBreed = pet.getBreed() != null
                || (pet.getBreedName() != null && !pet.getBreedName().isBlank());

        if (petTypeChanged && hasExistingBreed && !breedTouched) {
            throw GeneralException.of(PetErrorCode.PET_BREED_REQUIRED);
        }

        if (breedTouched) {
            breed = breedService.resolveBreed(req.breedId(), req.breed(), petType);
            breedName = breed != null ? breed.getName() : blankToNull(req.breed());
            if (breed == null && breedName == null) {
                throw GeneralException.of(PetErrorCode.PET_BREED_REQUIRED);
            }
        }

        boolean bloodTypeTouched = req.bloodType() != null && !req.bloodType().isBlank();
        String bloodType = bloodTypeTouched ? resolveBloodType(petType, req.bloodType()) : null;

        pet.update(
                req.petType(),
                breedTouched ? breed : null,
                breedTouched ? breedName : null,
                blankToNull(req.petName()),
                req.birth(),
                req.petWeight(),
                req.gender(),
                req.neutering(),
                bloodType,
                bloodTypeTouched
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

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
