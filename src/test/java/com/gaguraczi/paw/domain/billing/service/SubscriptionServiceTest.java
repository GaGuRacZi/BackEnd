package com.gaguraczi.paw.domain.billing.service;

import com.gaguraczi.paw.domain.billing.dto.req.PlanChangeReq;
import com.gaguraczi.paw.domain.billing.dto.res.SubscriptionRes;
import com.gaguraczi.paw.domain.billing.entity.PaymentHistory;
import com.gaguraczi.paw.domain.billing.entity.Subscription;
import com.gaguraczi.paw.domain.billing.enums.PaymentType;
import com.gaguraczi.paw.domain.billing.enums.SubscriptionStatus;
import com.gaguraczi.paw.domain.billing.exception.code.BillingErrorCode;
import com.gaguraczi.paw.domain.billing.repository.PaymentHistoryRepository;
import com.gaguraczi.paw.domain.billing.repository.SubscriptionRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    private static final ZoneId KST = ZoneId.of("Asia/Seoul");
    private static final Clock CLOCK = Clock.fixed(Instant.parse("2026-08-20T14:10:00Z"), KST);

    @Mock
    private SecurityUtils securityUtils;
    @Mock
    private UserRepository userRepository;
    @Mock
    private SubscriptionRepository subscriptionRepository;
    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService(
                securityUtils,
                userRepository,
                subscriptionRepository,
                paymentHistoryRepository,
                CLOCK
        );
    }

    @Test
    void 업그레이드는_즉시_적용하고_결제내역을_남기며_PRO는_코인_10개를_지급한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(3).subscribe(SubscribeType.BASIC).build();
        stubLockedUser(uid, user);
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));
        when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        SubscriptionRes res = subscriptionService.changePlan(new PlanChangeReq(SubscribeType.PRO));

        assertThat(user.currentPlan()).isEqualTo(SubscribeType.PRO);
        assertThat(user.coinBalance()).isEqualTo(13);
        assertThat(res.plan()).isEqualTo(SubscribeType.PRO);
        assertThat(res.periodEnd()).isEqualTo(LocalDateTime.of(2026, 9, 20, 23, 10));
        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getAmount()).isEqualTo(4_900);
        assertThat(captor.getValue().getType()).isEqualTo(PaymentType.PURCHASE);
    }

    @Test
    void 다운그레이드는_pending만_남기고_현재_플랜은_유지한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(8).subscribe(SubscribeType.PRO).build();
        Subscription subscription = paidSubscription(user, SubscribeType.PRO);
        stubLockedUser(uid, user);
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.of(subscription));

        SubscriptionRes res = subscriptionService.changePlan(new PlanChangeReq(SubscribeType.BASIC));

        assertThat(user.currentPlan()).isEqualTo(SubscribeType.PRO);
        assertThat(subscription.getPendingPlan()).isEqualTo(SubscribeType.BASIC);
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.PENDING_CHANGE);
        assertThat(res.pendingPlan()).isEqualTo(SubscribeType.BASIC);
        verify(paymentHistoryRepository, never()).save(any());
    }

    @Test
    void 같은_플랜_재요청은_예약된_다운그레이드를_취소한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).subscribe(SubscribeType.PRO).build();
        Subscription subscription = paidSubscription(user, SubscribeType.PRO);
        subscription.scheduleDowngrade(SubscribeType.BASIC);
        stubLockedUser(uid, user);
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.of(subscription));

        SubscriptionRes res = subscriptionService.changePlan(new PlanChangeReq(SubscribeType.PRO));

        assertThat(subscription.hasPendingChange()).isFalse();
        assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        assertThat(res.pendingPlan()).isNull();
    }

    @Test
    void 같은_플랜이고_예약이_없으면_400이다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).subscribe(SubscribeType.BASIC).build();
        stubLockedUser(uid, user);
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.empty());
        when(subscriptionRepository.save(any(Subscription.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThatThrownBy(() -> subscriptionService.changePlan(new PlanChangeReq(SubscribeType.BASIC)))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(BillingErrorCode.SAME_PLAN);
    }

    @Test
    void 스케줄러는_예약된_BASIC을_기간_종료_후_적용한다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(8).subscribe(SubscribeType.PRO).build();
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(SubscribeType.PRO)
                .periodStart(LocalDateTime.of(2026, 7, 20, 23, 10))
                .periodEnd(LocalDateTime.of(2026, 8, 20, 23, 10))
                .status(SubscriptionStatus.PENDING_CHANGE)
                .pendingPlan(SubscribeType.BASIC)
                .build();
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.of(subscription));

        subscriptionService.processDueOne(uid);

        assertThat(user.currentPlan()).isEqualTo(SubscribeType.BASIC);
        assertThat(user.coinBalance()).isEqualTo(8);
        assertThat(subscription.getPeriodEnd()).isNull();
        assertThat(subscription.hasPendingChange()).isFalse();
        verify(paymentHistoryRepository, never()).save(any());
    }

    @Test
    void 스케줄러는_PRO를_갱신하고_코인_10개와_결제내역을_남긴다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(2).subscribe(SubscribeType.PRO).build();
        Subscription subscription = Subscription.builder()
                .user(user)
                .plan(SubscribeType.PRO)
                .periodStart(LocalDateTime.of(2026, 7, 20, 23, 10))
                .periodEnd(LocalDateTime.of(2026, 8, 20, 23, 10))
                .status(SubscriptionStatus.ACTIVE)
                .build();
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.of(subscription));
        when(paymentHistoryRepository.save(any(PaymentHistory.class))).thenAnswer(inv -> inv.getArgument(0));

        subscriptionService.processDueOne(uid);

        assertThat(user.currentPlan()).isEqualTo(SubscribeType.PRO);
        assertThat(user.coinBalance()).isEqualTo(12);
        assertThat(subscription.getPeriodEnd()).isEqualTo(LocalDateTime.of(2026, 9, 20, 23, 10));
        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(PaymentType.RENEWAL);
        assertThat(captor.getValue().getAmount()).isEqualTo(4_900);
    }

    @Test
    void 관리자_강제는_즉시_BASIC으로_바꾸고_결제내역을_남기지_않는다() {
        UUID uid = UUID.randomUUID();
        User user = User.builder().uid(uid).coin(8).subscribe(SubscribeType.ULTIMATE).build();
        Subscription subscription = paidSubscription(user, SubscribeType.ULTIMATE);
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));
        when(subscriptionRepository.findByUserUidForUpdate(uid)).thenReturn(Optional.of(subscription));

        SubscriptionRes res = subscriptionService.forceChange(uid, SubscribeType.BASIC);

        assertThat(user.currentPlan()).isEqualTo(SubscribeType.BASIC);
        assertThat(res.plan()).isEqualTo(SubscribeType.BASIC);
        assertThat(subscription.getPeriodEnd()).isNull();
        verify(paymentHistoryRepository, never()).save(any());
    }

    private void stubLockedUser(UUID uid, User user) {
        when(securityUtils.currentUid()).thenReturn(uid);
        when(userRepository.findByIdForUpdate(uid)).thenReturn(Optional.of(user));
    }

    private static Subscription paidSubscription(User user, SubscribeType plan) {
        return Subscription.builder()
                .user(user)
                .plan(plan)
                .periodStart(LocalDateTime.of(2026, 8, 20, 23, 10))
                .periodEnd(LocalDateTime.of(2026, 9, 20, 23, 10))
                .status(SubscriptionStatus.ACTIVE)
                .build();
    }
}
