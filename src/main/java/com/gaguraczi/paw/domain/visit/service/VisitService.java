package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.medication.entity.Medication;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.repository.MedicationRepository;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.dto.res.RagSearchRes;
import com.gaguraczi.paw.domain.visit.client.VisitAiSummaryClient;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.dto.req.VisitCreateReq;
import com.gaguraczi.paw.domain.visit.dto.req.VisitPrescriptionAddReq;
import com.gaguraczi.paw.domain.visit.dto.res.VisitAiSummaryRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitCreateRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitDetailRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitListRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitPrescriptionRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitTranscriptRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitTranscriptTurnRes;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.entity.VisitPrescription;
import com.gaguraczi.paw.domain.visit.enums.PrescriptionSource;
import com.gaguraczi.paw.domain.visit.enums.TranscriptSpeaker;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitPrescriptionRepository;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.domain.visit.support.VisitAudioValidator;
import com.gaguraczi.paw.domain.visit.support.VisitPetDisplay;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.utils.S3.S3Dto;
import com.gaguraczi.paw.utils.S3.S3Utils;
import lombok.RequiredArgsConstructor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class VisitService {

    private static final String AUDIO_DIRECTORY = "visit-audio";

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final VisitRepository visitRepository;
    private final VisitPrescriptionRepository visitPrescriptionRepository;
    private final MedicationRepository medicationRepository;
    private final VisitAccessService visitAccessService;
    private final VisitAudioValidator visitAudioValidator;
    private final S3Utils s3Utils;
    private final VisitProcessService visitProcessService;
    private final VisitAiSummaryTxService visitAiSummaryTxService;
    private final VisitAiSummaryClient visitAiSummaryClient;
    private final VisitProperties visitProperties;
    private final Clock clock;

    @Transactional
    public VisitCreateRes create(VisitCreateReq req, MultipartFile audio) {
        User user = securityUtils.currentUser();
        if (req == null || req.petId() == null) {
            throw GeneralException.of(VisitErrorCode.VISIT_PET_REQUIRED);
        }
        Pet pet = visitAccessService.requireOwnedPet(req.petId(), user.getUid());
        int durationSec = visitAudioValidator.validateAndDurationSec(audio);
        S3Dto uploaded = s3Utils.uploadMultipartUnderDirectory(audio, AUDIO_DIRECTORY);
        try {
            Visit visit = Visit.builder()
                    .pet(pet)
                    .user(user)
                    .status(VisitStatus.PROCESSING)
                    .audioS3Key(uploaded.getKey())
                    .audioUrl(uploaded.getUrl())
                    .audioDurationSec(durationSec > 0 ? durationSec : null)
                    .audioContentType(audio.getContentType())
                    .build();
            visitRepository.save(visit);
            Long visitId = visit.getVisitId();
            afterCommitProcess(visitId, uploaded.getKey());
            return VisitCreateRes.from(visit);
        } catch (RuntimeException e) {
            s3Utils.deleteQuietly(uploaded.getKey());
            throw e;
        }
    }

    @Transactional(readOnly = true)
    public List<VisitListRes> list(Long petId) {
        User user = securityUtils.currentUser();
        visitAccessService.requireOwnedPet(petId, user.getUid());
        return visitRepository.findByPet_PetIdAndUser_UidOrderByCreatedAtDesc(petId, user.getUid())
                .stream()
                .map(VisitListRes::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public VisitDetailRes get(Long visitId) {
        return VisitDetailRes.from(ownedVisit(visitId), LocalDate.now(clock));
    }

    @Transactional(readOnly = true)
    public VisitTranscriptRes transcript(Long visitId) {
        Visit visit = ownedVisit(visitId);
        if (visit.getStatus() != VisitStatus.READY) {
            throw GeneralException.of(VisitErrorCode.VISIT_NOT_READY);
        }
        List<VisitTranscriptTurnRes> turns = visit.getTranscriptTurns().stream()
                .map(turn -> new VisitTranscriptTurnRes(
                        turn.getSpeaker(),
                        turn.getText(),
                        turn.getStartSec(),
                        turn.getEndSec()
                ))
                .toList();
        return new VisitTranscriptRes(
                visit.getVisitId(),
                visit.getHospitalName(),
                visit.getCreatedAt(),
                visit.getAudioUrl(),
                visit.getAudioDurationSec(),
                turns
        );
    }

    @Transactional
    public VisitPrescriptionRes addPrescription(Long visitId, VisitPrescriptionAddReq req) {
        Visit visit = ownedVisit(visitId);
        if (visit.getStatus() != VisitStatus.READY) {
            throw GeneralException.of(VisitErrorCode.VISIT_NOT_READY);
        }
        if (req == null || req.source() == null || req.frequency() == null || req.mealTiming() == null) {
            throw GeneralException.of(VisitErrorCode.VISIT_PRESCRIPTION_INVALID);
        }
        VisitPrescription prescription;
        if (req.source() == PrescriptionSource.CATALOG) {
            if (req.medicationId() == null) {
                throw GeneralException.of(VisitErrorCode.VISIT_PRESCRIPTION_INVALID);
            }
            Medication medication = medicationRepository.findById(req.medicationId())
                    .orElseThrow(() -> GeneralException.of(MedicationErrorCode.MEDICATION_NOT_FOUND));
            String caution = blankToNull(req.caution());
            if (caution == null) {
                caution = firstCautionLine(medication.getPrecautionMd());
            }
            prescription = VisitPrescription.builder()
                    .source(PrescriptionSource.CATALOG)
                    .medication(medication)
                    .nameKo(medication.getNameKo())
                    .nameEn(medication.getNameEn())
                    .ingredient(medication.getIngredient())
                    .dosageAmount(req.dosageAmount())
                    .dosageUnit(defaultUnit(req.dosageUnit()))
                    .frequency(req.frequency())
                    .mealTiming(req.mealTiming())
                    .takeTimes(req.takeTimes() == null ? new ArrayList<>() : new ArrayList<>(req.takeTimes()))
                    .caution(caution)
                    .build();
        } else {
            String nameKo = blankToNull(req.nameKo());
            if (nameKo == null) {
                throw GeneralException.of(VisitErrorCode.VISIT_PRESCRIPTION_INVALID);
            }
            prescription = VisitPrescription.builder()
                    .source(PrescriptionSource.CUSTOM)
                    .medication(null)
                    .nameKo(nameKo)
                    .nameEn(blankToNull(req.nameEn()))
                    .ingredient(blankToNull(req.ingredient()))
                    .dosageAmount(req.dosageAmount())
                    .dosageUnit(defaultUnit(req.dosageUnit()))
                    .frequency(req.frequency())
                    .mealTiming(req.mealTiming())
                    .takeTimes(req.takeTimes() == null ? new ArrayList<>() : new ArrayList<>(req.takeTimes()))
                    .caution(blankToNull(req.caution()))
                    .build();
        }
        prescription.attach(visit);
        visit.getPrescriptions().add(prescription);
        visitPrescriptionRepository.save(prescription);
        return VisitPrescriptionRes.from(prescription);
    }

    @Transactional
    public void deletePrescription(Long visitId, Long prescriptionId) {
        Visit visit = ownedVisit(visitId);
        VisitPrescription prescription = visitPrescriptionRepository
                .findByPrescriptionIdAndVisit_VisitId(prescriptionId, visit.getVisitId())
                .orElseThrow(() -> GeneralException.of(VisitErrorCode.VISIT_NOT_FOUND));
        visit.getPrescriptions().removeIf(item -> prescriptionId.equals(item.getPrescriptionId()));
        visitPrescriptionRepository.delete(prescription);
    }

    public VisitAiSummaryRes generateAiSummary(Long visitId) {
        User user = securityUtils.currentUser();
        Visit visit = ownedVisit(visitId);
        if (visit.isAiSummaryDone()) {
            return VisitAiSummaryRes.of(
                    visit.getVisitId(),
                    visit.getAiSummaryMd(),
                    user.coinBalance(),
                    user.usedCoinBalance(),
                    sourcesForResponse(List.of())
            );
        }
        int cost = visitProperties.getAiSummaryCoinCost();
        VisitAiSummaryTxService.ReserveResult reserved =
                visitAiSummaryTxService.reserve(visit.getVisitId(), user.getUid(), cost);
        if (reserved == VisitAiSummaryTxService.ReserveResult.ALREADY_DONE) {
            Visit reloaded = ownedVisit(visitId);
            User refreshed = userRepository.findById(user.getUid()).orElse(user);
            return VisitAiSummaryRes.of(
                    reloaded.getVisitId(),
                    reloaded.getAiSummaryMd(),
                    refreshed.coinBalance(),
                    refreshed.usedCoinBalance(),
                    sourcesForResponse(List.of())
            );
        }
        try {
            RagAskResult generated = visitAiSummaryClient.generate(buildAiSummaryInput(ownedVisit(visitId)));
            visitAiSummaryTxService.complete(visitId, generated.answer());
            User refreshed = userRepository.findById(user.getUid()).orElse(user);
            List<RagSearchRes> sources = generated.sources() == null
                    ? List.of()
                    : generated.sources().stream().map(RagSearchRes::from).toList();
            return VisitAiSummaryRes.of(
                    visitId,
                    generated.answer(),
                    refreshed.coinBalance(),
                    refreshed.usedCoinBalance(),
                    sourcesForResponse(sources)
            );
        } catch (RuntimeException e) {
            visitAiSummaryTxService.refund(visitId, user.getUid(), cost);
            if (e instanceof GeneralException) {
                throw e;
            }
            throw GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED, e);
        }
    }

    private Visit ownedVisit(Long visitId) {
        return visitAccessService.requireOwnedVisit(visitId, securityUtils.currentUser().getUid());
    }

    private List<RagSearchRes> sourcesForResponse(List<RagSearchRes> sources) {
        if (!visitProperties.isAiSummaryIncludeSources()) {
            return null;
        }
        return sources == null ? List.of() : sources;
    }

    private void afterCommitProcess(Long visitId, String s3Key) {
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            submitProcess(visitId);
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                submitProcess(visitId);
            }

            @Override
            public void afterCompletion(int status) {
                if (status != STATUS_COMMITTED) {
                    s3Utils.deleteQuietly(s3Key);
                }
            }
        });
    }

    private void submitProcess(Long visitId) {
        try {
            visitProcessService.processAsync(visitId);
        } catch (TaskRejectedException e) {
            visitProcessService.handleSubmitRejected(visitId);
        }
    }

    private String buildAiSummaryInput(Visit visit) {
        StringBuilder sb = new StringBuilder();
        Pet pet = visit.getPet();
        sb.append("반려동물: ").append(pet.getPetName());
        String breed = VisitPetDisplay.breedName(pet);
        if (breed != null) {
            sb.append(", ").append(breed);
        }
        String age = VisitPetDisplay.ageLabel(pet.getBirth(), LocalDate.now(clock));
        if (age != null) {
            sb.append(", ").append(age);
        }
        sb.append('\n');
        if (visit.getVisitName() != null) {
            sb.append("진료명: ").append(visit.getVisitName()).append('\n');
        }
        if (visit.getOneLineSummary() != null) {
            sb.append("한줄요약: ").append(visit.getOneLineSummary()).append('\n');
        }
        sb.append("\n전사문:\n");
        for (var turn : visit.getTranscriptTurns()) {
            String label = turn.getSpeaker() == TranscriptSpeaker.VET ? "의사" : "보호자";
            sb.append(label).append(": ").append(turn.getText()).append('\n');
        }
        sb.append("\n처방 약물:\n");
        if (visit.getPrescriptions().isEmpty()) {
            sb.append("(없음)\n");
        } else {
            for (VisitPrescription prescription : visit.getPrescriptions()) {
                sb.append("- ").append(prescription.getNameKo());
                if (prescription.getNameEn() != null) {
                    sb.append(" (").append(prescription.getNameEn()).append(')');
                }
                sb.append(" / ").append(prescription.getFrequency());
                sb.append(" / ").append(prescription.getMealTiming());
                if (prescription.getCaution() != null) {
                    sb.append(" / 주의: ").append(prescription.getCaution());
                }
                sb.append('\n');
            }
        }
        return sb.toString();
    }

    private static String defaultUnit(String unit) {
        String trimmed = blankToNull(unit);
        return trimmed == null ? "정" : trimmed;
    }

    private static String firstCautionLine(String precautionMd) {
        if (precautionMd == null || precautionMd.isBlank()) {
            return null;
        }
        for (String line : precautionMd.split("\\R")) {
            String trimmed = line.replaceFirst("^[-*•]\\s*", "").trim();
            if (!trimmed.isEmpty()) {
                return trimmed;
            }
        }
        return null;
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
