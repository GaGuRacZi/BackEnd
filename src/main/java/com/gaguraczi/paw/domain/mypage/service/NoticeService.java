package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.res.NoticeDetailRes;
import com.gaguraczi.paw.domain.mypage.dto.res.NoticeListItemRes;
import com.gaguraczi.paw.domain.mypage.entity.Notice;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.NoticeRepository;
import com.gaguraczi.paw.domain.mypage.support.MypageCursorCodec;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NoticeService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final NoticeRepository noticeRepository;
    private final Clock clock;

    public CursorPageRes<NoticeListItemRes> search(String keyword, String cursor, Integer size) {
        int pageSize = normalizeSize(size);
        MypageCursorCodec.Cursor decoded = MypageCursorCodec.decode(cursor);
        String normalizedKeyword = keyword == null || keyword.isBlank() ? null : keyword.trim();

        List<Notice> rows = noticeRepository.search(
                normalizedKeyword,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                PageRequest.of(0, pageSize + 1)
        );

        boolean hasNext = rows.size() > pageSize;
        List<Notice> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<NoticeListItemRes> content = page.stream()
                .map(notice -> NoticeListItemRes.from(notice, LocalDate.now(clock)))
                .toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? MypageCursorCodec.encode(page.getLast().getCreatedAt(), page.getLast().getNoticeId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    @Transactional
    public NoticeDetailRes getDetail(Long noticeId) {
        int updated = noticeRepository.increaseViewCount(noticeId);
        if (updated == 0) {
            throw GeneralException.of(MypageErrorCode.NOTICE_NOT_FOUND);
        }
        Notice notice = noticeRepository.findById(noticeId)
                .orElseThrow(() -> GeneralException.of(MypageErrorCode.NOTICE_NOT_FOUND));
        return NoticeDetailRes.from(notice);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
