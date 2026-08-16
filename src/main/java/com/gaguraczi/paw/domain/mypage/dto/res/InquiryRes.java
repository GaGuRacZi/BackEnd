package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;

import java.time.LocalDateTime;
import java.util.List;

public record InquiryRes(
        Long inquiryId,
        InquiryType inquiryType,
        String content,
        List<String> attachmentUrls,
        InquiryStatus status,
        String answer,
        LocalDateTime createdAt
) {
    public static InquiryRes from(Inquiry inquiry) {
        return new InquiryRes(
                inquiry.getInquiryId(),
                inquiry.getInquiryType(),
                inquiry.getContent(),
                inquiry.getAttachmentUrls(),
                inquiry.getStatus(),
                inquiry.getAnswer(),
                inquiry.getCreatedAt()
        );
    }
}
