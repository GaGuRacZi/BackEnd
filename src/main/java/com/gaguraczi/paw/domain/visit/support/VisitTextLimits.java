package com.gaguraczi.paw.domain.visit.support;

public final class VisitTextLimits {

    private VisitTextLimits() {
    }

    public static boolean inRange(String text, int minInclusive, int maxInclusive) {
        if (text == null) {
            return false;
        }
        int length = text.length();
        return length >= minInclusive && length <= maxInclusive;
    }
}
