package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

public record NoticeListItemRes(
        Long noticeId,
        String title,
        boolean isNew,
        LocalDateTime createdAt
) {
    private static final int NEW_BADGE_DAYS = 7;

    public static NoticeListItemRes from(Notice notice) {
        boolean isNew = notice.getCreatedAt() != null
                && notice.getCreatedAt().toLocalDate().isAfter(LocalDate.now().minusDays(NEW_BADGE_DAYS));
        return new NoticeListItemRes(notice.getNoticeId(), notice.getTitle(), isNew, notice.getCreatedAt());
    }
}
