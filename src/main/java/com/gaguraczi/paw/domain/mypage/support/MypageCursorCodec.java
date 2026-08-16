package com.gaguraczi.paw.domain.mypage.support;

import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

/** 공지사항/문의 등 createdAt+id 단순 최신순 목록 전용 커서 코덱 */
public final class MypageCursorCodec {

    private MypageCursorCodec() {
    }

    public record Cursor(LocalDateTime createdAt, Long id) {
    }

    public static String encode(LocalDateTime createdAt, Long id) {
        String raw = createdAt + "|" + id;
        return Base64.getUrlEncoder().withoutPadding().encodeToString(raw.getBytes(StandardCharsets.UTF_8));
    }

    public static Cursor decode(String cursor) {
        if (cursor == null || cursor.isBlank()) {
            return null;
        }
        try {
            String raw = new String(Base64.getUrlDecoder().decode(cursor), StandardCharsets.UTF_8);
            String[] parts = raw.split("\\|", 2);
            return new Cursor(LocalDateTime.parse(parts[0]), Long.parseLong(parts[1]));
        } catch (Exception e) {
            throw GeneralException.of(MypageErrorCode.MYPAGE_400);
        }
    }
}
