package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitAccessServiceTest {

    @Mock
    private PetRepository petRepository;
    @Mock
    private VisitRepository visitRepository;

    private VisitAccessService visitAccessService;

    @BeforeEach
    void setUp() {
        visitAccessService = new VisitAccessService(petRepository, visitRepository);
    }

    @Test
    void hidesOtherUsersPetAsNotFound() {
        UUID ownerUid = UUID.randomUUID();
        UUID otherUid = UUID.randomUUID();
        User other = User.builder().uid(otherUid).build();
        Pet pet = pet(1L, other, "아리");
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThatThrownBy(() -> visitAccessService.requireOwnedPet(1L, ownerUid))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(PetErrorCode.PET_NOT_FOUND);
    }

    @Test
    void hidesMissingPetAsNotFound() {
        when(petRepository.findById(9L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitAccessService.requireOwnedPet(9L, UUID.randomUUID()))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(PetErrorCode.PET_NOT_FOUND);
    }

    @Test
    void returnsOwnedPet() {
        UUID uid = UUID.randomUUID();
        User owner = User.builder().uid(uid).build();
        Pet pet = pet(1L, owner, "아리");
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));

        assertThat(visitAccessService.requireOwnedPet(1L, uid)).isSameAs(pet);
    }

    @Test
    void hidesMissingVisitAsNotFound() {
        UUID uid = UUID.randomUUID();
        when(visitRepository.findByVisitIdAndUser_Uid(7L, uid)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitAccessService.requireOwnedVisit(7L, uid))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_NOT_FOUND);
    }

    @Test
    void hidesOwnershipMismatchVisitAsNotFound() {
        UUID uid = UUID.randomUUID();
        UUID otherUid = UUID.randomUUID();
        User owner = User.builder().uid(otherUid).build();
        User requester = User.builder().uid(uid).build();
        Pet pet = pet(1L, owner, "아리");
        Visit visit = Visit.builder()
                .visitId(7L)
                .pet(pet)
                .user(requester)
                .status(VisitStatus.READY)
                .build();
        when(visitRepository.findByVisitIdAndUser_Uid(7L, uid)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> visitAccessService.requireOwnedVisit(7L, uid))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_NOT_FOUND);
    }

    @Test
    void returnsOwnedVisit() {
        UUID uid = UUID.randomUUID();
        User owner = User.builder().uid(uid).build();
        Pet pet = pet(1L, owner, "아리");
        Visit visit = Visit.builder()
                .visitId(7L)
                .pet(pet)
                .user(owner)
                .status(VisitStatus.READY)
                .build();
        when(visitRepository.findByVisitIdAndUser_Uid(7L, uid)).thenReturn(Optional.of(visit));

        assertThat(visitAccessService.requireOwnedVisit(7L, uid).getVisitId()).isEqualTo(7L);
    }

    private static Pet pet(Long petId, User user, String name) {
        return Pet.builder()
                .petId(petId)
                .user(user)
                .petName(name)
                .birth(LocalDate.of(2015, 3, 1))
                .petWeight(new BigDecimal("4.20"))
                .gender(Gender.FEMALE)
                .build();
    }
}
