package com.gaguraczi.paw.domain.visit.support;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonLocation;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.gaguraczi.paw.domain.visit.exception.code.VisitErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.io.CharArrayReader;

public final class VisitJsonText {

    private static final JsonFactory JSON_FACTORY = new JsonFactory();

    private VisitJsonText() {
    }

    public static String extractJson(String raw) {
        if (raw == null || raw.isBlank()) {
            throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
        }
        String text = stripCodeFences(raw);
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c != '{' && c != '[') {
                continue;
            }
            try (JsonParser parser = JSON_FACTORY.createParser(new CharArrayReader(chars, i, chars.length - i))) {
                JsonToken token = parser.nextToken();
                if (token != JsonToken.START_OBJECT && token != JsonToken.START_ARRAY) {
                    continue;
                }
                parser.skipChildren();
                JsonLocation end = parser.currentLocation();
                long offset = end.getCharOffset();
                if (offset <= 0) {
                    continue;
                }
                int endIndex = Math.min(chars.length - i, (int) offset);
                String extracted = text.substring(i, i + endIndex).trim();
                if (!extracted.isEmpty()) {
                    return extracted;
                }
            } catch (Exception ignored) {
                // try the next '{' or '['
            }
        }
        throw GeneralException.of(VisitErrorCode.VISIT_SUMMARY_FAILED);
    }

    private static String stripCodeFences(String raw) {
        String trimmed = raw.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        int contentStart = 3;
        while (contentStart < trimmed.length() && !Character.isWhitespace(trimmed.charAt(contentStart))) {
            contentStart++;
        }
        if (contentStart < trimmed.length() && trimmed.charAt(contentStart) == '\r') {
            contentStart++;
        }
        if (contentStart < trimmed.length() && trimmed.charAt(contentStart) == '\n') {
            contentStart++;
        }
        int fence = trimmed.lastIndexOf("```");
        if (fence <= contentStart) {
            return trimmed.substring(contentStart).trim();
        }
        return trimmed.substring(contentStart, fence).trim();
    }
}
