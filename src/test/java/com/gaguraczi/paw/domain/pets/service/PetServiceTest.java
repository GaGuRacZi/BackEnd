package com.gaguraczi.paw.domain.pets.service;

import com.gaguraczi.paw.domain.breed.service.BreedService;
import com.gaguraczi.paw.domain.pets.dto.req.PetCreateReq;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.PetType;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Utils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private BreedService breedService;
    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private S3Utils s3Utils;

    @InjectMocks
    private PetService petService;

    private final User user = User.builder().uid(UUID.randomUUID()).build();

    @Test
    void 품종_없이_등록하면_예외가_발생한다() {
        when(securityUtils.currentUser()).thenReturn(user);
        when(breedService.resolveBreed(any(), any(), any())).thenReturn(null);

        PetCreateReq req = new PetCreateReq(
                PetType.DOG, null, null, "초코", LocalDate.of(2022, 1, 1),
                BigDecimal.valueOf(3.5), Gender.MALE, true
        );

        assertThatThrownBy(() -> petService.create(req, null))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(PetErrorCode.PET_BREED_REQUIRED);
    }

    @Test
    void 대표펫_삭제시_남은_펫_중_최근등록순으로_대표가_재지정된다() {
        Pet mainPet = Pet.builder().petId(1L).user(user).petType(PetType.DOG).isMain(true).build();
        Pet nextPet = Pet.builder().petId(2L).user(user).petType(PetType.DOG).isMain(false).build();

        when(securityUtils.currentUser()).thenReturn(user);
        when(petRepository.findById(1L)).thenReturn(Optional.of(mainPet));
        when(petRepository.findFirstByUserAndPetIdNotOrderByCreatedAtDesc(user, 1L))
                .thenReturn(Optional.of(nextPet));

        petService.delete(1L);

        assertThat(nextPet.isMain()).isTrue();
    }
}
