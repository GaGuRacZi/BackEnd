package com.gaguraczi.paw.domain.users.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 플랜. BASIC=꼬마 젤리(0원/코인 3), PRO=새싹 젤리(4900원/월 10코인), ULTIMATE=어른 젤리(9900원/무제한)")
public enum SubscribeType {
    @Schema(description = "꼬마 젤리")
    BASIC("꼬마 젤리", 0, 0, false, 0),
    @Schema(description = "새싹 젤리")
    PRO("새싹 젤리", 4_900, 10, false, 1),
    @Schema(description = "어른 젤리")
    ULTIMATE("어른 젤리", 9_900, 0, true, 2);

    public static final int BASIC_INCLUDED_COINS = 3;

    private final String displayName;
    private final int priceWon;
    private final int monthlyCoinGrant;
    private final boolean unlimitedCoins;
    private final int rank;

    SubscribeType(String displayName, int priceWon, int monthlyCoinGrant, boolean unlimitedCoins, int rank) {
        this.displayName = displayName;
        this.priceWon = priceWon;
        this.monthlyCoinGrant = monthlyCoinGrant;
        this.unlimitedCoins = unlimitedCoins;
        this.rank = rank;
    }

    public String displayName() {
        return displayName;
    }

    public int priceWon() {
        return priceWon;
    }

    /** 매월 지급 코인. 없으면 null */
    public Integer monthlyCoinGrant() {
        return monthlyCoinGrant > 0 ? monthlyCoinGrant : null;
    }

    public int monthlyCoinGrantOrZero() {
        return monthlyCoinGrant;
    }

    public boolean unlimitedCoins() {
        return unlimitedCoins;
    }

    public boolean isPaid() {
        return priceWon > 0;
    }

    public int rank() {
        return rank;
    }

    public boolean isUpgradeFrom(SubscribeType current) {
        return rank > current.rank;
    }

    public boolean isDowngradeFrom(SubscribeType current) {
        return rank < current.rank;
    }

    /** BASIC 가입 지급량 또는 PRO 월 지급량. 무제한은 null */
    public Integer includedCoins() {
        if (unlimitedCoins) {
            return null;
        }
        if (this == BASIC) {
            return BASIC_INCLUDED_COINS;
        }
        return monthlyCoinGrant();
    }
}
