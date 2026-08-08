package com.gaguraczi.paw.domain.community.support;

import com.gaguraczi.paw.domain.community.exception.code.CommunityErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

public final class CommentCursorCodec {

    private CommentCursorCodec() {
    }

    public record Cursor(LocalDateTime createdAt, Long commentId) {
    }

    public static String encode(LocalDateTime createdAt, Long commentId) {
        long epoch = createdAt.toInstant(ZoneOffset.UTC).toEpochMilli();
        String raw = epoch + "_" + commentId;
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("_", 2);
            if (parts.length != 2) {
                throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
            }
            return new Cursor(
                    LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(parts[0])), ZoneOffset.UTC),
                    Long.parseLong(parts[1])
            );
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(CommunityErrorCode.INVALID_CURSOR_400);
        }
    }
}
