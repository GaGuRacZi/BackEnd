package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.entity.VisitPrescription;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VisitAccessService {

    private final PetRepository petRepository;
    private final VisitRepository visitRepository;

    public Pet requireOwnedPet(Long petId, UUID uid) {
        Pet pet = petRepository.findById(petId)
                .orElseThrow(() -> GeneralException.of(PetErrorCode.PET_NOT_FOUND));
        if (!pet.getUser().getUid().equals(uid)) {
            throw GeneralException.of(PetErrorCode.PET_NOT_FOUND);
        }
        return pet;
    }

    @Transactional(readOnly = true)
    public Visit requireOwnedVisit(Long visitId, UUID uid) {
        Visit visit = visitRepository.findByVisitIdAndUser_Uid(visitId, uid)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        if (!visit.getPet().getUser().getUid().equals(uid)) {
            throw GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND);
        }
        initialize(visit);
        return visit;
    }

    private static void initialize(Visit visit) {
        Hibernate.initialize(visit.getPet());
        Pet pet = visit.getPet();
        if (pet.getBreed() != null) {
            Hibernate.initialize(pet.getBreed());
        }
        Hibernate.initialize(pet.getUser());
        Hibernate.initialize(visit.getUser());
        if (visit.getPrescriptions() != null) {
            Hibernate.initialize(visit.getPrescriptions());
            for (VisitPrescription prescription : visit.getPrescriptions()) {
                if (prescription.getMedication() != null) {
                    Hibernate.initialize(prescription.getMedication());
                }
            }
        }
        if (visit.getTranscriptTurns() != null) {
            Hibernate.initialize(visit.getTranscriptTurns());
        }
    }
}
