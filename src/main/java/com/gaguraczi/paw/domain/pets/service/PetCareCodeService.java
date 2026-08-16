package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.pets.dto.res.PetCareCodeRes;
import com.gaguraczi.paw.domain.pets.entity.PetCareCode;
import com.gaguraczi.paw.domain.pets.entity.PetCareSelection;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.pets.repository.PetCareCodeRepository;
import com.gaguraczi.paw.domain.pets.repository.PetCareSelectionRepository;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PetCareCodeService {

    private final PetCareCodeRepository petCareCodeRepository;
    private final PetCareSelectionRepository petCareSelectionRepository;
    private final PetRepository petRepository;
    private final SecurityUtils securityUtils;

    public List<PetCareCodeRes> searchCodes(PetCareCategory category, PetType species, String keyword) {
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();
        return petCareCodeRepository.search(category, species, normalizedKeyword).stream()
                .map(PetCareCodeRes::from)
                .toList();
    }

    public List<PetCareCodeRes> getSelections(Long petId, PetCareCategory category) {
        Pet pet = findOwnedPet(petId);
        return petCareSelectionRepository.findByPetAndCode_Category(pet, category).stream()
                .map(selection -> PetCareCodeRes.from(selection.getCode()))
                .toList();
    }

    @Transactional
    public List<PetCareCodeRes> replaceSelections(Long petId, PetCareCategory category, List<Long> codeIds) {
        Pet pet = findOwnedPet(petId);
        List<Long> distinctCodeIds = codeIds.stream().distinct().toList();

        List<PetCareCode> codes = petCareCodeRepository.findAllById(distinctCodeIds);
        if (codes.size() != distinctCodeIds.size()) {
            throw GeneralException.of(PetErrorCode.PET_CODE_NOT_FOUND);
        }
        boolean categoryMismatch = codes.stream().anyMatch(code -> code.getCategory() != category);
        if (categoryMismatch) {
            throw GeneralException.of(PetErrorCode.PET_CODE_NOT_FOUND);
        }

        petCareSelectionRepository.deleteByPetAndCode_Category(pet, category);
        List<PetCareSelection> selections = codes.stream()
                .map(code -> buildSelection(pet, code))
                .toList();
        petCareSelectionRepository.saveAll(selections);

        return codes.stream().map(PetCareCodeRes::from).toList();
    }

    private static PetCareSelection buildSelection(Pet pet, PetCareCode code) {
        return PetCareSelection.builder().pet(pet).code(code).build();
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
