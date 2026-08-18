package com.gaguraczi.paw.domain.visit.support;

import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

public final class VisitJsonText {

    private VisitJsonText() {
    }

    public static String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
        }
        String trimmed = raw.trim();
        int arrayStart = trimmed.indexOf('[');
        int objectStart = trimmed.indexOf('{');
        if (arrayStart >= 0 && (objectStart < 0 || arrayStart < objectStart)) {
            int end = trimmed.lastIndexOf(']');
            if (end <= arrayStart) {
                throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
            }
            return trimmed.substring(arrayStart, end + 1);
        }
        int end = trimmed.lastIndexOf('}');
        if (objectStart < 0 || end <= objectStart) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
        }
        return trimmed.substring(objectStart, end + 1);
    }
}
