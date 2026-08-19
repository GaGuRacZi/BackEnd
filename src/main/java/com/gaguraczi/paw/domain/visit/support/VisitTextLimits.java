package com.gaguraczi.paw.domain.visit.support;

public final class VisitTextLimits {

    public static final int SPEAKER_PROMPT_MAX_CHARS = 8000;

    private VisitTextLimits() {
    }

    public static boolean inRange(String text, int minInclusive, int maxInclusive) {
        if (text == null) {
            return false;
        }
        int length = text.length();
        return length >= minInclusive && length <= maxInclusive;
    }

    public static String truncate(String text, int maxInclusive) {
        if (text == null) {
            return "";
        }
        if (maxInclusive < 0 || text.length() <= maxInclusive) {
            return text;
        }
        return text.substring(0, maxInclusive);
    }
}
