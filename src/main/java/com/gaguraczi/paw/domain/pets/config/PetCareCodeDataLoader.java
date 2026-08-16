package com.gaguraczi.paw.domain.pets.config;

import com.gaguraczi.paw.domain.pets.entity.PetCareCode;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.pets.repository.PetCareCodeRepository;
import com.gaguraczi.paw.domain.users.enums.PetType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Component
@Order(60)
@RequiredArgsConstructor
public class PetCareCodeDataLoader implements ApplicationRunner {

    private final PetCareCodeRepository petCareCodeRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (petCareCodeRepository.existsByCategory(PetCareCategory.INGREDIENT)
                && petCareCodeRepository.existsByCategory(PetCareCategory.SURGERY)
                && petCareCodeRepository.existsByCategory(PetCareCategory.CARE_AREA)) {
            return;
        }

        List.of(
                code(PetCareCategory.INGREDIENT, null, "감"),
                code(PetCareCategory.INGREDIENT, null, "감자"),
                code(PetCareCategory.INGREDIENT, null, "강낭콩"),
                code(PetCareCategory.INGREDIENT, null, "닭고기"),
                code(PetCareCategory.INGREDIENT, null, "밀"),
                code(PetCareCategory.INGREDIENT, null, "우유"),
                code(PetCareCategory.INGREDIENT, null, "포도"),

                code(PetCareCategory.SURGERY, PetType.DOG, "탈장 수술"),
                code(PetCareCategory.SURGERY, PetType.DOG, "고관절 수술"),
                code(PetCareCategory.SURGERY, null, "척추 디스크 수술"),
                code(PetCareCategory.SURGERY, PetType.DOG, "슬개골 탈구 수술"),
                code(PetCareCategory.SURGERY, PetType.CAT, "요로결석 수술"),
                code(PetCareCategory.SURGERY, null, "중성화 수술"),

                code(PetCareCategory.CARE_AREA, null, "간"),
                code(PetCareCategory.CARE_AREA, null, "노화"),
                code(PetCareCategory.CARE_AREA, null, "눈"),
                code(PetCareCategory.CARE_AREA, null, "피부"),
                code(PetCareCategory.CARE_AREA, null, "관절"),
                code(PetCareCategory.CARE_AREA, null, "신장"),
                code(PetCareCategory.CARE_AREA, null, "체중")
        ).forEach(this::saveIfAbsent);

        log.info("Pet care code seed ensured. count={}", petCareCodeRepository.count());
    }

    private PetCareCode code(PetCareCategory category, PetType species, String name) {
        return PetCareCode.builder()
                .category(category)
                .species(species)
                .name(name)
                .build();
    }

    private void saveIfAbsent(PetCareCode code) {
        boolean exists = petCareCodeRepository.search(code.getCategory(), code.getSpecies(), code.getName()).stream()
                .anyMatch(existing -> existing.getName().equals(code.getName()) && existing.getSpecies() == code.getSpecies());
        if (!exists) {
            petCareCodeRepository.save(code);
        }
    }
}
