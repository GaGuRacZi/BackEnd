package com.gaguraczi.paw.domain.chat.support;

import com.gaguraczi.paw.domain.chat.exception.code.ChatErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** 메시지 목록은 최신 → 과거 방향으로 messageId 기준 커서 페이지네이션한다. */
public final class ChatMessageCursorCodec {

    private ChatMessageCursorCodec() {
    }

    public static String encode(Long messageId) {
        return Base64.getUrlEncoder().withoutPadding()
                .encodeToString(String.valueOf(messageId).getBytes(StandardCharsets.UTF_8));
    }

    public static Long decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            return Long.parseLong(raw);
        } catch (Exception e) {
            throw GeneralException.of(ChatErrorCode.INVALID_CURSOR_400);
        }
    }
}
