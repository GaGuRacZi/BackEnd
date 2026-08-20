package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.medication.entity.Medication;
import com.gaguraczi.paw.domain.medication.exception.code.MedicationErrorCode;
import com.gaguraczi.paw.domain.medication.repository.MedicationRepository;
import com.gaguraczi.paw.domain.pets.exception.code.PetErrorCode;
import com.gaguraczi.paw.domain.rag.dto.RagAskResult;
import com.gaguraczi.paw.domain.rag.dto.RagSearchHit;
import com.gaguraczi.paw.domain.rag.enums.RagSourceType;
import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.domain.visit.client.VisitAiSummaryClient;
import com.gaguraczi.paw.domain.visit.config.VisitProperties;
import com.gaguraczi.paw.domain.visit.dto.req.VisitPrescriptionAddReq;
import com.gaguraczi.paw.domain.visit.dto.res.VisitAiSummaryRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitListRes;
import com.gaguraczi.paw.domain.visit.dto.res.VisitPrescriptionRes;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.DoseFrequency;
import com.gaguraczi.paw.domain.visit.enums.MealTiming;
import com.gaguraczi.paw.domain.visit.enums.PrescriptionSource;
import com.gaguraczi.paw.domain.visit.enums.VisitStatus;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.domain.visit.repository.VisitPrescriptionRepository;
import com.gaguraczi.paw.domain.visit.repository.VisitRepository;
import com.gaguraczi.paw.domain.visit.support.VisitAudioValidator;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.global.time.AppTime;
import com.gaguraczi.paw.utils.S3.S3Utils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisitServiceTest {

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private VisitRepository visitRepository;
    @Mock
    private VisitPrescriptionRepository visitPrescriptionRepository;
    @Mock
    private MedicationRepository medicationRepository;
    @Mock
    private VisitAccessService visitAccessService;
    @Mock
    private VisitAudioValidator visitAudioValidator;
    @Mock
    private S3Utils s3Utils;
    @Mock
    private VisitProcessService visitProcessService;
    @Mock
    private VisitAiSummaryTxService visitAiSummaryTxService;
    @Mock
    private VisitAiSummaryClient visitAiSummaryClient;

    private VisitService visitService;
    private VisitProperties visitProperties;
    private final Clock clock = Clock.fixed(java.time.Instant.parse("2026-08-20T20:00:00Z"), AppTime.KST);
    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        visitProperties = new VisitProperties();
        visitService = new VisitService(
                securityUtils,
                userRepository,
                visitRepository,
                visitPrescriptionRepository,
                medicationRepository,
                visitAccessService,
                visitAudioValidator,
                s3Utils,
                visitProcessService,
                visitAiSummaryTxService,
                visitAiSummaryClient,
                visitProperties,
                clock
        );
        user = User.builder().uid(UUID.randomUUID()).coin(4).usedCoin(1).build();
        pet = pet(user);
        when(securityUtils.currentUser()).thenReturn(user);
    }

    @Test
    void listRejectsOtherUsersPet() {
        when(visitAccessService.requireOwnedPet(99L, user.getUid()))
                .thenThrow(GeneralException.of(PetErrorCode.PET_NOT_FOUND));

        assertThatThrownBy(() -> visitService.list(99L))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(PetErrorCode.PET_NOT_FOUND);
        verify(visitRepository, never()).findByPet_PetIdAndUser_UidOrderByCreatedAtDesc(any(), any());
    }

    @Test
    void listHidesTitleWhileProcessing() {
        when(visitAccessService.requireOwnedPet(1L, user.getUid())).thenReturn(pet);
        Visit processing = Visit.builder()
                .visitId(3L)
                .pet(pet)
                .user(user)
                .status(VisitStatus.PROCESSING)
                .visitName("보이면 안 됨")
                .oneLineSummary("요약 중")
                .build();
        when(visitRepository.findByPet_PetIdAndUser_UidOrderByCreatedAtDesc(1L, user.getUid()))
                .thenReturn(List.of(processing));

        List<VisitListRes> result = visitService.list(1L);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().visitName()).isNull();
        assertThat(result.getFirst().oneLineSummary()).isNull();
        assertThat(result.getFirst().status()).isEqualTo(VisitStatus.PROCESSING);
    }

    @Test
    void doesNotRechargeWhenAiSummaryAlreadyDone() {
        Visit visit = readyVisit();
        visit.completeAiSummary("# 기존 요약", LocalDateTime.of(2026, 8, 21, 5, 0));
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);

        VisitAiSummaryRes res = visitService.generateAiSummary(11L);

        assertThat(res.aiSummaryMd()).isEqualTo("# 기존 요약");
        assertThat(res.coin()).isEqualTo(4);
        assertThat(res.usedCoin()).isEqualTo(1);
        assertThat(res.sources()).isEmpty();
        verify(visitAiSummaryTxService, never()).reserve(any(), any(), anyInt());
        verify(visitAiSummaryClient, never()).generate(any());
    }

    @Test
    void returnsFileSearchSourcesOnGenerate() {
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        when(visitAiSummaryTxService.reserve(11L, user.getUid(), 1))
                .thenReturn(VisitAiSummaryTxService.ReserveResult.RESERVED);
        RagSearchHit hit = new RagSearchHit(
                "file-1",
                "내과_QA_000.md",
                "SRC-1",
                0,
                RagSourceType.QA,
                "내과",
                "노령견",
                "관절염",
                null,
                "앞다리를 절어요",
                0.91
        );
        when(visitAiSummaryClient.generate(any()))
                .thenReturn(new RagAskResult("# 요약", List.of(hit)));
        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));

        VisitAiSummaryRes res = visitService.generateAiSummary(11L);

        assertThat(res.aiSummaryMd()).isEqualTo("# 요약");
        assertThat(res.sources()).hasSize(1);
        assertThat(res.sources().getFirst().sourceId()).isEqualTo("SRC-1");
        assertThat(res.sources().getFirst().department()).isEqualTo("내과");
        verify(visitAiSummaryTxService).complete(11L, "# 요약");
    }

    @Test
    void omitsSourcesWhenDisabled() {
        visitProperties.setAiSummaryIncludeSources(false);
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        when(visitAiSummaryTxService.reserve(11L, user.getUid(), 1))
                .thenReturn(VisitAiSummaryTxService.ReserveResult.RESERVED);
        RagSearchHit hit = new RagSearchHit(
                "file-1",
                "내과_QA_000.md",
                "SRC-1",
                0,
                RagSourceType.QA,
                "내과",
                "노령견",
                "관절염",
                null,
                "앞다리를 절어요",
                0.91
        );
        when(visitAiSummaryClient.generate(any()))
                .thenReturn(new RagAskResult("# 요약", List.of(hit)));
        when(userRepository.findById(user.getUid())).thenReturn(Optional.of(user));

        VisitAiSummaryRes res = visitService.generateAiSummary(11L);

        assertThat(res.aiSummaryMd()).isEqualTo("# 요약");
        assertThat(res.sources()).isNull();
    }

    @Test
    void refundsWhenAiSummaryGenerationFails() {
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        when(visitAiSummaryTxService.reserve(11L, user.getUid(), 1))
                .thenReturn(VisitAiSummaryTxService.ReserveResult.RESERVED);
        when(visitAiSummaryClient.generate(any())).thenThrow(GeneralException.of(VisitErrorCode.VISIT_AI_SUMMARY_FAILED));

        assertThatThrownBy(() -> visitService.generateAiSummary(11L))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AI_SUMMARY_FAILED);
        verify(visitAiSummaryTxService).refund(11L, user.getUid(), 1);
        verify(visitAiSummaryTxService, never()).complete(any(), any());
    }

    @Test
    void addsCustomPrescriptionWithoutMedicationMaster() {
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        when(visitPrescriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VisitPrescriptionRes res = visitService.addPrescription(11L, new VisitPrescriptionAddReq(
                PrescriptionSource.CUSTOM,
                null,
                "관절영양제",
                null,
                null,
                1,
                "정",
                DoseFrequency.TWICE_DAILY,
                MealTiming.AFTER_MEAL,
                List.of(),
                null
        ));

        assertThat(res.source()).isEqualTo(PrescriptionSource.CUSTOM);
        assertThat(res.medicationId()).isNull();
        assertThat(res.nameKo()).isEqualTo("관절영양제");
        verify(medicationRepository, never()).findById(any());
    }

    @Test
    void catalogPrescriptionRequiresExistingMedication() {
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        when(medicationRepository.findById(88L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> visitService.addPrescription(11L, new VisitPrescriptionAddReq(
                PrescriptionSource.CATALOG,
                88L,
                null,
                null,
                null,
                1,
                "정",
                DoseFrequency.ONCE_DAILY,
                MealTiming.AFTER_MEAL,
                List.of(),
                null
        )))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MedicationErrorCode.MEDICATION_NOT_FOUND);
    }

    @Test
    void catalogPrescriptionCopiesMasterName() {
        Visit visit = readyVisit();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);
        Medication medication = Medication.builder()
                .medicationId(5L)
                .itemSeq("1")
                .nameKo("카미녹스")
                .nameEn("Carprofen")
                .ingredient("카르프로펜")
                .descriptionMd("설명")
                .precautionMd("- 위장장애에 주의하세요")
                .searchText("카미녹스")
                .build();
        when(medicationRepository.findById(5L)).thenReturn(Optional.of(medication));
        when(visitPrescriptionRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        VisitPrescriptionRes res = visitService.addPrescription(11L, new VisitPrescriptionAddReq(
                PrescriptionSource.CATALOG,
                5L,
                "무시됨",
                null,
                null,
                1,
                "정",
                DoseFrequency.ONCE_DAILY,
                MealTiming.AFTER_MEAL,
                List.of(),
                null
        ));

        assertThat(res.source()).isEqualTo(PrescriptionSource.CATALOG);
        assertThat(res.nameKo()).isEqualTo("카미녹스");
        assertThat(res.caution()).isEqualTo("위장장애에 주의하세요");
    }

    @Test
    void rejectsPrescriptionBeforeReady() {
        Visit visit = Visit.builder()
                .visitId(11L)
                .pet(pet)
                .user(user)
                .status(VisitStatus.PROCESSING)
                .build();
        when(visitAccessService.requireOwnedVisit(11L, user.getUid())).thenReturn(visit);

        assertThatThrownBy(() -> visitService.addPrescription(11L, new VisitPrescriptionAddReq(
                PrescriptionSource.CUSTOM,
                null,
                "영양제",
                null,
                null,
                1,
                "정",
                DoseFrequency.ONCE_DAILY,
                MealTiming.ANYTIME,
                List.of(),
                null
        )))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_NOT_READY);
    }

    private Visit readyVisit() {
        return Visit.builder()
                .visitId(11L)
                .pet(pet)
                .user(user)
                .status(VisitStatus.READY)
                .visitName("관절염 정기 진료")
                .oneLineSummary("퇴행성 관절염 진단")
                .build();
    }

    private static Pet pet(User user) {
        return Pet.builder()
                .petId(1L)
                .user(user)
                .petName("아리")
                .birth(LocalDate.of(2015, 3, 1))
                .petWeight(new BigDecimal("4.20"))
                .gender(Gender.FEMALE)
                .breedName("말티즈")
                .build();
    }
}
