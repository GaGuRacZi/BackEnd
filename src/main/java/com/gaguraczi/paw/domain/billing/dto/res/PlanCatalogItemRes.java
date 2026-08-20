package com.gaguraczi.paw.domain.billing.dto.res;

import com.gaguraczi.paw.domain.users.enums.SubscribeType;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "요금제 카탈로그 항목")
public record PlanCatalogItemRes(
        @Schema(description = "플랜 코드", example = "PRO")
        SubscribeType plan,
        @Schema(description = "화면 표시명", example = "새싹 젤리")
        String displayName,
        @Schema(description = "월 가격(원)", example = "4900")
        int priceWon,
        @Schema(description = "매월 지급 코인. 없으면 null", example = "10")
        Integer monthlyCoinGrant,
        @Schema(description = "포함 코인(BASIC 가입 3 / PRO 월 10). 무제한은 null", example = "10")
        Integer includedCoins,
        @Schema(description = "코인 무제한 여부", example = "false")
        boolean unlimitedCoin
) {
    public static PlanCatalogItemRes from(SubscribeType type) {
        return new PlanCatalogItemRes(
                type,
                type.displayName(),
                type.priceWon(),
                type.monthlyCoinGrant(),
                type.includedCoins(),
                type.unlimitedCoins()
        );
    }
}
