package com.gaguraczi.paw.domain.chat.support;

import com.gaguraczi.paw.domain.chat.exception.code.ChatErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/** 채팅방 목록은 lastMessageAt 내림차순, 동률이면 roomId 내림차순으로 커서 페이지네이션한다. */
public final class ChatRoomCursorCodec {

    private ChatRoomCursorCodec() {
    }

    public record Cursor(LocalDateTime lastMessageAt, Long roomId) {
    }

    public static String encode(LocalDateTime lastMessageAt, Long roomId) {
        String raw = lastMessageAt + "_" + roomId;
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
                throw GeneralException.of(ChatErrorCode.INVALID_CURSOR_400);
            }
            return new Cursor(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (GeneralException e) {
            throw e;
        } catch (Exception e) {
            throw GeneralException.of(ChatErrorCode.INVALID_CURSOR_400);
        }
    }
}
