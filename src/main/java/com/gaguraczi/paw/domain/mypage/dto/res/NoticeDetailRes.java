package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Notice;

import java.time.LocalDateTime;

public record NoticeDetailRes(
        Long noticeId,
        String title,
        String content,
        Long viewCount,
        LocalDateTime createdAt
) {
    public static NoticeDetailRes from(Notice notice) {
        return new NoticeDetailRes(
                notice.getNoticeId(),
                notice.getTitle(),
                notice.getContent(),
                notice.getViewCount(),
                notice.getCreatedAt()
        );
    }
}
