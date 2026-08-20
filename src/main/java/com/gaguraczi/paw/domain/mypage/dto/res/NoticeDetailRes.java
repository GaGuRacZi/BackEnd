package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Notice;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "공지사항 상세. 조회 시 viewCount가 1 증가한 뒤 반환됩니다.")
public record NoticeDetailRes(
        @Schema(description = "공지 ID", example = "1")
        Long noticeId,
        @Schema(description = "제목", example = "서비스 점검 안내")
        String title,
        @Schema(description = "본문", example = "8월 21일 02:00~04:00 점검이 예정되어 있습니다.")
        String content,
        @Schema(description = "조회수 (이번 요청 반영 후)", example = "42")
        Long viewCount,
        @Schema(description = "등록 시각", example = "2026-08-18T09:00:00")
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
