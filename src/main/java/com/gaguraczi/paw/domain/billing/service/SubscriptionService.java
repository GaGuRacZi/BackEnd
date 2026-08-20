package com.gaguraczi.paw.domain.billing.service;

import com.gaguraczi.paw.domain.billing.dto.req.PlanChangeReq;
import com.gaguraczi.paw.domain.billing.dto.res.PaymentHistoryItemRes;
import com.gaguraczi.paw.domain.billing.dto.res.SubscriptionRes;
import com.gaguraczi.paw.domain.billing.entity.PaymentHistory;
import com.gaguraczi.paw.domain.billing.entity.Subscription;
import com.gaguraczi.paw.domain.billing.enums.PaymentStatus;
import com.gaguraczi.paw.domain.billing.enums.PaymentType;
import com.gaguraczi.paw.domain.billing.enums.SubscriptionStatus;
import com.gaguraczi.paw.domain.billing.exception.code.BillingErrorCode;
import com.gaguraczi.paw.domain.billing.repository.PaymentHistoryRepository;
import com.gaguraczi.paw.domain.billing.repository.SubscriptionRepository;
import com.gaguraczi.paw.domain.mypage.support.MypageCursorCodec;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SubscriptionService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final SecurityUtils securityUtils;
    private final UserRepository userRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final Clock clock;

    @Transactional
    public SubscriptionRes getCurrent() {
        User user = lockedCurrentUser();
        return SubscriptionRes.of(user, getOrCreate(user));
    }

    @Transactional
    public SubscriptionRes changePlan(PlanChangeReq req) {
        User user = lockedCurrentUser();
        Subscription subscription = getOrCreate(user);
        SubscribeType current = user.currentPlan();
        SubscribeType target = req.plan();

        if (target == current) {
            if (subscription.hasPendingChange()) {
                subscription.cancelPending();
                return SubscriptionRes.of(user, subscription);
            }
            throw GeneralException.of(BillingErrorCode.SAME_PLAN);
        }

        if (target.isUpgradeFrom(current)) {
            mockPay(user, target, PaymentType.PURCHASE);
            applyPlanNow(user, subscription, target, true);
            return SubscriptionRes.of(user, subscription);
        }

        if (target.isDowngradeFrom(current)) {
            if (subscription.getPeriodEnd() == null || !subscription.getPeriodEnd().isAfter(now())) {
                applyPlanNow(user, subscription, target, true);
                if (target.isPaid()) {
                    mockPay(user, target, PaymentType.PURCHASE);
                }
            } else {
                subscription.scheduleDowngrade(target);
            }
            return SubscriptionRes.of(user, subscription);
        }

        throw GeneralException.of(BillingErrorCode.SAME_PLAN);
    }

    @Transactional
    public SubscriptionRes forceChange(UUID uid, SubscribeType plan) {
        User user = userRepository.findByIdForUpdate(uid)
                .orElseThrow(() -> GeneralException.of(BillingErrorCode.USER_NOT_FOUND));
        Subscription subscription = getOrCreate(user);
        SubscribeType previous = user.currentPlan();
        boolean grantPro = plan == SubscribeType.PRO && previous != SubscribeType.PRO;
        applyPlanNow(user, subscription, plan, grantPro);
        return SubscriptionRes.of(user, subscription);
    }

    @Transactional
    public int processDue() {
        List<Subscription> due = subscriptionRepository.findDue(now());
        int processed = 0;
        for (Subscription row : due) {
            processDueOne(row.getUser().getUid());
            processed++;
        }
        return processed;
    }

    @Transactional
    public void processDueOne(UUID uid) {
        User user = userRepository.findByIdForUpdate(uid)
                .orElse(null);
        if (user == null || user.isDeleted()) {
            return;
        }
        Subscription subscription = subscriptionRepository.findByUserUidForUpdate(uid)
                .orElse(null);
        if (subscription == null) {
            return;
        }
        LocalDateTime periodEnd = subscription.getPeriodEnd();
        if (periodEnd == null || periodEnd.isAfter(now())) {
            return;
        }

        if (subscription.hasPendingChange()) {
            SubscribeType pending = subscription.getPendingPlan();
            applyPlanNow(user, subscription, pending, true);
            if (pending.isPaid()) {
                mockPay(user, pending, PaymentType.PURCHASE);
            }
            return;
        }

        SubscribeType current = user.currentPlan();
        if (!current.isPaid()) {
            subscription.applyPlan(SubscribeType.BASIC, null, null);
            user.updateSubscribe(SubscribeType.BASIC);
            return;
        }

        mockPay(user, current, PaymentType.RENEWAL);
        applyPlanNow(user, subscription, current, true);
    }

    public CursorPageRes<PaymentHistoryItemRes> getPayments(String cursor, Integer size) {
        User user = securityUtils.currentUser();
        int pageSize = normalizeSize(size);
        MypageCursorCodec.Cursor decoded = MypageCursorCodec.decode(cursor);

        List<PaymentHistory> rows = paymentHistoryRepository.findMyPayments(
                user.getUid(),
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<PaymentHistory> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<PaymentHistoryItemRes> content = page.stream().map(PaymentHistoryItemRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? MypageCursorCodec.encode(page.getLast().getCreatedAt(), page.getLast().getPaymentId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public PaymentHistoryItemRes getPayment(Long paymentId) {
        User user = securityUtils.currentUser();
        PaymentHistory payment = paymentHistoryRepository.findByPaymentIdAndUser_Uid(paymentId, user.getUid())
                .orElseThrow(() -> GeneralException.of(BillingErrorCode.PAYMENT_NOT_FOUND));
        return PaymentHistoryItemRes.from(payment);
    }

    private User lockedCurrentUser() {
        return userRepository.findByIdForUpdate(securityUtils.currentUid())
                .orElseThrow(() -> GeneralException.of(BillingErrorCode.USER_NOT_FOUND));
    }

    private Subscription getOrCreate(User user) {
        return subscriptionRepository.findByUserUidForUpdate(user.getUid())
                .orElseGet(() -> createDefault(user));
    }

    private Subscription createDefault(User user) {
        SubscribeType plan = user.currentPlan();
        LocalDateTime start = plan.isPaid() ? now() : null;
        Subscription created = Subscription.builder()
                .user(user)
                .plan(plan)
                .status(SubscriptionStatus.ACTIVE)
                .periodStart(start)
                .periodEnd(start == null ? null : start.plusMonths(1))
                .build();
        try {
            return subscriptionRepository.save(created);
        } catch (DataIntegrityViolationException e) {
            return subscriptionRepository.findByUserUidForUpdate(user.getUid())
                    .orElseThrow(() -> e);
        }
    }

    private void applyPlanNow(User user, Subscription subscription, SubscribeType plan, boolean grantIfPro) {
        user.updateSubscribe(plan);
        if (plan.isPaid()) {
            LocalDateTime start = now();
            subscription.applyPlan(plan, start, start.plusMonths(1));
        } else {
            subscription.applyPlan(plan, null, null);
        }
        if (grantIfPro && plan == SubscribeType.PRO) {
            user.grantCoin(SubscribeType.PRO.monthlyCoinGrantOrZero());
        }
    }

    private void mockPay(User user, SubscribeType plan, PaymentType type) {
        paymentHistoryRepository.save(PaymentHistory.builder()
                .user(user)
                .plan(plan)
                .amount(plan.priceWon())
                .type(type)
                .status(PaymentStatus.SUCCESS)
                .paidAt(now())
                .build());
    }

    private LocalDateTime now() {
        return LocalDateTime.now(clock);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
