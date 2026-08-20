package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Schema(description = "공지사항 목록 아이템")
public record NoticeListItemRes(
        @Schema(description = "공지 ID", example = "1")
        Long noticeId,
        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,
        @Schema(description = "등록일 기준 7일 이내면 true (NEW 뱃지)", example = "true")
        boolean isNew,
        @Schema(description = "등록 시각", example = "2026-08-18T09:00:00")
        LocalDateTime createdAt
) {
    private static final int NEW_BADGE_DAYS = 7;

    public static NoticeListItemRes from(Notice notice, LocalDate today) {
        boolean isNew = notice.getCreatedAt() != null
                && notice.getCreatedAt().toLocalDate().isAfter(today.minusDays(NEW_BADGE_DAYS));
        return new NoticeListItemRes(notice.getNoticeId(), notice.getTitle(), isNew, notice.getCreatedAt());
    }
}
