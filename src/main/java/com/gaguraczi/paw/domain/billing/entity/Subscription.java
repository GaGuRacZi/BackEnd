package com.gaguraczi.paw.domain.billing.entity;

import com.gaguraczi.paw.domain.billing.enums.SubscriptionStatus;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;

@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "subscription",
        indexes = @Index(name = "idx_subscription_period_end", columnList = "period_end")
)
public class Subscription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "subscription_id")
    private Long subscriptionId;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false, unique = true)
    private User user;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "plan", nullable = false, length = 20)
    private SubscribeType plan = SubscribeType.BASIC;

    @Enumerated(EnumType.STRING)
    @Column(name = "pending_plan", length = 20)
    private SubscribeType pendingPlan;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    @Column(name = "status", nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    public void applyPlan(SubscribeType plan, LocalDateTime periodStart, LocalDateTime periodEnd) {
        this.plan = plan;
        this.pendingPlan = null;
        this.periodStart = periodStart;
        this.periodEnd = periodEnd;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void scheduleDowngrade(SubscribeType pendingPlan) {
        this.pendingPlan = pendingPlan;
        this.status = SubscriptionStatus.PENDING_CHANGE;
    }

    public void cancelPending() {
        this.pendingPlan = null;
        this.status = SubscriptionStatus.ACTIVE;
    }

    public boolean hasPendingChange() {
        return pendingPlan != null;
    }
}
