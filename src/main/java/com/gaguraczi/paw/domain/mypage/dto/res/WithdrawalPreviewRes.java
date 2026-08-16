package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.users.enums.SubscribeType;

public record WithdrawalPreviewRes(
        boolean subscribing,
        SubscribeType subscribePlan,
        boolean hasOngoingMarketTrade
) {
}
