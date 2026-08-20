package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.InquiryAnswerReq;
import com.gaguraczi.paw.domain.mypage.dto.res.AdminInquiryRes;
import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.InquiryRepository;
import com.gaguraczi.paw.domain.mypage.support.MypageCursorCodec;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminInquiryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final InquiryRepository inquiryRepository;

    public CursorPageRes<AdminInquiryRes> getInquiries(
            String cursor,
            Integer size,
            InquiryStatus status,
            InquiryType inquiryType
    ) {
        int pageSize = normalizeSize(size);
        MypageCursorCodec.Cursor decoded = MypageCursorCodec.decode(cursor);
        List<Inquiry> rows = inquiryRepository.findAllForAdmin(
                status,
                inquiryType,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                PageRequest.of(0, pageSize + 1)
        );
        boolean hasNext = rows.size() > pageSize;
        List<Inquiry> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<AdminInquiryRes> content = page.stream().map(AdminInquiryRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? MypageCursorCodec.encode(page.getLast().getCreatedAt(), page.getLast().getInquiryId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public AdminInquiryRes getDetail(Long inquiryId) {
        return AdminInquiryRes.from(getOrThrow(inquiryId));
    }

    @Transactional
    public AdminInquiryRes answer(Long inquiryId, InquiryAnswerReq req) {
        Inquiry inquiry = getOrThrow(inquiryId);
        inquiry.answer(req.answer());
        return AdminInquiryRes.from(inquiry);
    }

    private Inquiry getOrThrow(Long inquiryId) {
        return inquiryRepository.findByIdWithUser(inquiryId)
                .orElseThrow(() -> GeneralException.of(MypageErrorCode.INQUIRY_NOT_FOUND));
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
