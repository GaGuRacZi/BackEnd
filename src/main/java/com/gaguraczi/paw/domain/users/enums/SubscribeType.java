package com.gaguraczi.paw.domain.users.enums;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "구독 플랜. BASIC=꼬마 젤리, PRO=새싹 젤리, ULTIMATE=어른 젤리")
public enum SubscribeType {
    @Schema(description = "꼬마 젤리")
    BASIC("꼬마 젤리"),
    @Schema(description = "새싹 젤리")
    PRO("새싹 젤리"),
    @Schema(description = "어른 젤리")
    ULTIMATE("어른 젤리");

    private final String displayName;

    SubscribeType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
