package com.gaguraczi.paw.domain.users.enums;

public enum SubscribeType {
    BASIC("꼬마 젤리"),
    PRO("새싹 젤리"),
    ULTIMATE("어른 젤리");

    private final String displayName;

    SubscribeType(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }
}
