package com.gaguraczi.paw.domain.billing.dto.res;

import com.gaguraczi.paw.domain.billing.entity.Subscription;
import com.gaguraczi.paw.domain.billing.enums.SubscriptionStatus;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@Schema(description = "현재 구독/요금제")
public record SubscriptionRes(
        @Schema(description = "현재 플랜", example = "PRO")
        SubscribeType plan,
        @Schema(description = "화면 표시명", example = "새싹 젤리")
        String displayName,
        @Schema(description = "월 가격(원)", example = "4900")
        int priceWon,
        @Schema(description = "매월 지급 코인. 없으면 null", example = "10")
        Integer monthlyCoinGrant,
        @Schema(description = "코인 무제한 여부", example = "false")
        boolean unlimitedCoin,
        @Schema(description = "남은 코인", example = "8")
        int coin,
        @Schema(description = "현재 결제 기간 시작. BASIC이면 null", example = "2026-08-20T23:10:00")
        LocalDateTime periodStart,
        @Schema(description = "다음 결제일(기간 종료). BASIC이면 null", example = "2026-09-20T23:10:00")
        LocalDateTime periodEnd,
        @Schema(description = "예약된 다운그레이드 플랜. 없으면 null", example = "BASIC")
        SubscribeType pendingPlan,
        @Schema(description = "예약 플랜 표시명. 없으면 null", example = "꼬마 젤리")
        String pendingDisplayName,
        @Schema(description = "구독 상태", example = "ACTIVE")
        SubscriptionStatus status,
        @Schema(description = "전체 요금제 목록")
        List<PlanCatalogItemRes> plans
) {
    public static SubscriptionRes of(User user, Subscription subscription) {
        SubscribeType plan = user.currentPlan();
        SubscribeType pending = subscription.getPendingPlan();
        return new SubscriptionRes(
                plan,
                plan.displayName(),
                plan.priceWon(),
                plan.monthlyCoinGrant(),
                user.hasUnlimitedCoins(),
                user.coinBalance(),
                subscription.getPeriodStart(),
                subscription.getPeriodEnd(),
                pending,
                pending == null ? null : pending.displayName(),
                subscription.getStatus(),
                Arrays.stream(SubscribeType.values()).map(PlanCatalogItemRes::from).toList()
        );
    }
}
