package com.gaguraczi.paw.domain.community.support;

import com.gaguraczi.paw.domain.community.enums.CommunitySort;
import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.time.AppTime;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.Base64;

public final class CommunityCursorCodec {

    private CommunityCursorCodec() {
    }

    public record Cursor(
            CommunitySort sort,
            LocalDateTime createdAt,
            Long sortValue,
            Long postId
    ) {
    }

    public static String encodeLatest(LocalDateTime createdAt, Long postId) {
        return encodeRaw(CommunitySort.LATEST.name() + "|" + createdAt + "|" + postId);
    }

    public static String encodeByCount(CommunitySort sort, long sortValue, Long postId) {
        if (sort == null || sort == CommunitySort.LATEST) {
            throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
        }
        return encodeRaw(sort.name() + "|" + sortValue + "|" + postId);
    }

    public static Cursor decode(String cursor, CommunitySort expectedSort) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 3);
            if (parts.length != 3) {
                // backward-compatible: old format epoch_postId (LATEST only)
                return decodeLegacy(raw, expectedSort);
            }
            CommunitySort sort = CommunitySort.valueOf(parts[0]);
            if (expectedSort != null && sort != expectedSort) {
                throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
            }
            Long postId = Long.parseLong(parts[2]);
            if (sort == CommunitySort.LATEST) {
                LocalDateTime createdAt = parseCreatedAt(parts[1]);
                return new Cursor(sort, createdAt, null, postId);
            }
            return new Cursor(sort, null, Long.parseLong(parts[1]), postId);
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
        }
    }

    private static LocalDateTime parseCreatedAt(String value) {
        try {
            return LocalDateTime.parse(value);
        } catch (DateTimeParseException ignored) {
            return LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(value)), AppTime.KST);
        }
    }

    private static Cursor decodeLegacy(String raw, CommunitySort expectedSort) {
        if (expectedSort != null && expectedSort != CommunitySort.LATEST) {
            throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
        }
        String[] parts = raw.split("_", 2);
        if (parts.length != 2) {
            throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
        }
        LocalDateTime createdAt = LocalDateTime.ofInstant(
                Instant.ofEpochMilli(Long.parseLong(parts[0])),
                AppTime.KST);
        return new Cursor(CommunitySort.LATEST, createdAt, null, Long.parseLong(parts[1]));
    }

    private static String encodeRaw(String raw) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }
}
