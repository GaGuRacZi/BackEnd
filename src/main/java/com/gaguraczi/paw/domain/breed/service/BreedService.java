package com.gaguraczi.paw.domain.breed.service;

import com.gaguraczi.paw.domain.breed.dto.res.BreedRes;
import com.gaguraczi.paw.domain.breed.entity.Breed;
import com.gaguraczi.paw.domain.breed.exception.code.BreedErrorCode;
import com.gaguraczi.paw.domain.breed.repository.BreedRepository;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class BreedService {

    private final BreedRepository breedRepository;

    public List<BreedRes> search(PetType petType, String q, boolean popularOnly) {
        if (petType == null) {
            throw GeneralException.of(BreedErrorCode.BREED_TYPE_REQUIRED);
        }

        String keyword = q == null ? "" : q.trim();
        List<Breed> breeds = popularOnly
                ? breedRepository.searchPopular(petType, keyword)
                : breedRepository.search(petType, keyword);

        return breeds.stream().map(BreedRes::from).toList();
    }

    public Breed requireBreed(Long breedId, PetType petType) {
        Breed breed = breedRepository.findById(breedId)
                .orElseThrow(() -> GeneralException.of(BreedErrorCode.BREED_NOT_FOUND));
        if (breed.getPetType() != petType) {
            throw GeneralException.of(BreedErrorCode.BREED_TYPE_MISMATCH);
        }
        return breed;
    }

    /**
     * breedId 우선, 없으면 품종명으로 마스터 매핑.
     * 마스터에 없으면 null을 반환하고 호출측에서 breedName만 저장할 수 있다.
     */
    public Breed resolveBreed(Long breedId, String breedName, PetType petType) {
        if (breedId != null) {
            return requireBreed(breedId, petType);
        }
        if (breedName == null || breedName.isBlank()) {
            return null;
        }
        return breedRepository.findByPetTypeAndName(petType, breedName.trim()).orElse(null);
    }
}
