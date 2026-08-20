package com.gaguraczi.paw.domain.visit.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.Gender;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.domain.visit.entity.Visit;
import com.gaguraczi.paw.domain.visit.enums.AiSummaryStatus;
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
class VisitAiSummaryTxServiceTest {

    @Mock
    private VisitRepository visitRepository;
    @Mock
    private UserRepository userRepository;

    private VisitAiSummaryTxService txService;

    @BeforeEach
    void setUp() {
        txService = new VisitAiSummaryTxService(visitRepository, userRepository);
    }

    @Test
    void deductsCoinAndMarksGenerating() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(3).usedCoin(1).build();
        Visit visit = readyVisit(user);
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        VisitAiSummaryTxService.ReserveResult result = txService.reserve(11L, uid, 1);

        assertThat(result).isEqualTo(VisitAiSummaryTxService.ReserveResult.RESERVED);
        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.GENERATING);
        assertThat(user.coinBalance()).isEqualTo(2);
        assertThat(user.usedCoinBalance()).isEqualTo(2);
    }

    @Test
    void rejectsInsufficientCoin() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(0).usedCoin(2).build();
        Visit visit = readyVisit(user);
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> txService.reserve(11L, uid, 1))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_COIN_INSUFFICIENT);
        assertThat(user.coinBalance()).isZero();
        assertThat(user.usedCoinBalance()).isEqualTo(2);
    }

    @Test
    void doesNotChargeWhenSummaryAlreadyDone() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(5).usedCoin(0).build();
        Visit visit = readyVisit(user);
        visit.completeAiSummary("이미 생성된 요약");
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));

        VisitAiSummaryTxService.ReserveResult result = txService.reserve(11L, uid, 1);

        assertThat(result).isEqualTo(VisitAiSummaryTxService.ReserveResult.ALREADY_DONE);
        assertThat(user.coinBalance()).isEqualTo(5);
        assertThat(user.usedCoinBalance()).isZero();
    }

    @Test
    void rejectsConcurrentGenerating() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(5).usedCoin(0).build();
        Visit visit = readyVisit(user);
        visit.markAiSummaryGenerating();
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> txService.reserve(11L, uid, 1))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_AI_SUMMARY_CONFLICT);
    }

    @Test
    void rejectsWhenVisitNotReady() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(5).usedCoin(0).build();
        Visit visit = Visit.builder()
                .visitId(11L)
                .pet(pet(user))
                .user(user)
                .status(VisitStatus.PROCESSING)
                .build();
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));

        assertThatThrownBy(() -> txService.reserve(11L, uid, 1))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(VisitErrorCode.VISIT_NOT_READY);
    }

    @Test
    void refundsCoinAndResetsGenerating() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(2).usedCoin(2).build();
        Visit visit = readyVisit(user);
        visit.markAiSummaryGenerating();
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        txService.refund(11L, uid, 1);

        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.NONE);
        assertThat(user.coinBalance()).isEqualTo(3);
        assertThat(user.usedCoinBalance()).isEqualTo(1);
    }

    @Test
    void skipsRefundWhenStatusIsNone() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(2).usedCoin(2).build();
        Visit visit = readyVisit(user);
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        txService.refund(11L, uid, 1);

        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.NONE);
        assertThat(user.coinBalance()).isEqualTo(2);
        assertThat(user.usedCoinBalance()).isEqualTo(2);
    }

    @Test
    void skipsRefundWhenStatusIsDone() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(2).usedCoin(2).build();
        Visit visit = readyVisit(user);
        visit.completeAiSummary("# 완료");
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        txService.refund(11L, uid, 1);

        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.DONE);
        assertThat(user.coinBalance()).isEqualTo(2);
        assertThat(user.usedCoinBalance()).isEqualTo(2);
    }

    @Test
    void completePersistsMarkdown() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(2).usedCoin(2).build();
        Visit visit = readyVisit(user);
        visit.markAiSummaryGenerating();
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));

        txService.complete(11L, "# 요약");

        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.DONE);
        assertThat(visit.getAiSummaryMd()).isEqualTo("# 요약");
        assertThat(visit.getAiSummaryGeneratedAt()).isNotNull();
    }

    @Test
    void ultimate는_코인을_차감하지_않는다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(3).usedCoin(0).subscribe(SubscribeType.ULTIMATE).build();
        Visit visit = readyVisit(user);
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        VisitAiSummaryTxService.ReserveResult result = txService.reserve(11L, uid, 1);

        assertThat(result).isEqualTo(VisitAiSummaryTxService.ReserveResult.RESERVED);
        assertThat(user.coinBalance()).isEqualTo(3);
        assertThat(user.usedCoinBalance()).isZero();
    }

    @Test
    void ultimate는_환불에서도_코인을_건드리지_않는다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(3).usedCoin(0).subscribe(SubscribeType.ULTIMATE).build();
        Visit visit = readyVisit(user);
        visit.markAiSummaryGenerating();
        when(visitRepository.findByIdForUpdate(11L)).thenReturn(Optional.of(visit));
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));

        txService.refund(11L, uid, 1);

        assertThat(visit.getAiSummaryStatus()).isEqualTo(AiSummaryStatus.NONE);
        assertThat(user.coinBalance()).isEqualTo(3);
        assertThat(user.usedCoinBalance()).isZero();
    }

    private static Visit readyVisit(User user) {
        return Visit.builder()
                .visitId(11L)
                .pet(pet(user))
                .user(user)
                .status(VisitStatus.READY)
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
                .build();
    }
}
