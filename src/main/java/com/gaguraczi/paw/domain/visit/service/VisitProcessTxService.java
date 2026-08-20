package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.entity.VisitTranscriptTurn;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.hibernate.Hibernate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class VisitProcessTxService {

    private final VisitRepository visitRepository;

    @Transactional(readOnly = true)
    public Visit requireForProcessing(Long visitId) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        Hibernate.initialize(visit.getPet());
        Pet pet = visit.getPet();
        if (pet.getBreed() != null) {
            Hibernate.initialize(pet.getBreed());
        }
        Hibernate.initialize(visit.getUser());
        return visit;
    }

    @Transactional
    public void saveReady(
            Long visitId,
            List<VisitSpeakerMapper.MappedTurn> turns,
            VisitShortSummary summary,
            Integer durationSec
    ) {
        Visit visit = visitRepository.findById(visitId)
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        List<VisitTranscriptTurn> entities = new ArrayList<>();
        for (VisitSpeakerMapper.MappedTurn turn : turns) {
            entities.add(VisitTranscriptTurn.builder()
                    .speaker(turn.speaker())
                    .text(turn.text())
                    .startSec(turn.startSec())
                    .endSec(turn.endSec())
                    .sortOrder(turn.sortOrder())
                    .build());
        }
        visit.replaceTranscriptTurns(entities);
        visit.applyShortSummary(
                summary.visitName(),
                summary.oneLineSummary(),
                summary.diagnosisFindings(),
                summary.careItems(),
                summary.careNote(),
                summary.hospitalName(),
                durationSec
        );
    }

    @Transactional
    public void markFailed(Long visitId, String reason) {
        visitRepository.findById(visitId).ifPresent(visit -> visit.markFailed(reason));
    }

    @Transactional(readOnly = true)
    public Optional<NotifyTarget> loadNotifyTarget(Long visitId) {
        return visitRepository.findById(visitId).map(visit -> {
            Hibernate.initialize(visit.getUser());
            Hibernate.initialize(visit.getPet());
            return new NotifyTarget(
                    visit.getUser(),
                    visit.getVisitId(),
                    visit.getPet().getPetId()
            );
        });
    }

    public record NotifyTarget(User user, Long visitId, Long petId) {
    }
}
