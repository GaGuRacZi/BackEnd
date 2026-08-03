package com.gaguraczi.paw.domain.region.enums;

public enum RegionLevel {
    SIDO,
    SIGUNGU,
    DONG;

    public static RegionLevel fromCode(String code) {
        if (code == null || code.length() != 10) {
            throw new IllegalArgumentException("법정동코드는 10자리여야 합니다: " + code);
        }
        if (code.endsWith("00000000")) {
            return SIDO;
        }
        if (code.endsWith("00000")) {
            return SIGUNGU;
        }
        return DONG;
    }

    public static String parentCodeOf(String code, RegionLevel level) {
        return switch (level) {
            case SIDO -> null;
            case SIGUNGU -> code.substring(0, 2) + "00000000";
            case DONG -> code.substring(0, 5) + "00000";
        };
    }
}
