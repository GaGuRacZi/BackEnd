package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.pets.entity.PetCareCode;
import com.gaguraczi.paw.domain.pets.enums.PetCareCategory;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.pets.repository.PetCareCodeRepository;
import com.gaguraczi.paw.domain.pets.repository.PetCareSelectionRepository;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetCareCodeServiceTest {

    @Mock
    private PetCareCodeRepository petCareCodeRepository;
    @Mock
    private PetCareSelectionRepository petCareSelectionRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private SecurityUtils securityUtils;

    @InjectMocks
    private PetCareCodeService petCareCodeService;

    private final User user = User.builder().uid(UUID.randomUUID()).build();
    private final Pet pet = Pet.builder().petId(1L).user(user).build();

    @Test
    void 다른_카테고리의_코드ID가_섞이면_예외가_발생한다() {
        when(securityUtils.currentUser()).thenReturn(user);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        PetCareCode surgeryCode = PetCareCode.builder()
                .petCareCodeId(10L)
                .category(PetCareCategory.SURGERY)
                .name("탈장 수술")
                .build();
        when(petCareCodeRepository.findAllById(List.of(10L))).thenReturn(List.of(surgeryCode));

        assertThatThrownBy(() -> petCareCodeService.replaceSelections(1L, PetCareCategory.INGREDIENT, List.of(10L)))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(PetErrorCode.PET_CODE_NOT_FOUND);
    }

    @Test
    void 존재하지_않는_코드ID가_섞이면_예외가_발생한다() {
        when(securityUtils.currentUser()).thenReturn(user);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
        when(petCareCodeRepository.findAllById(List.of(999L))).thenReturn(List.of());

        assertThatThrownBy(() -> petCareCodeService.replaceSelections(1L, PetCareCategory.INGREDIENT, List.of(999L)))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(PetErrorCode.PET_CODE_NOT_FOUND);
    }
}
