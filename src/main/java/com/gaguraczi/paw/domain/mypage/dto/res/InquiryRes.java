package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "문의 내역")
public record InquiryRes(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "문의 유형", example = "PAYMENT")
        InquiryType inquiryType,
        @Schema(description = "문의 내용", example = "구독 결제가 반복해서 실패해요.")
        String content,
        @Schema(description = "첨부 파일 URL 목록")
        List<String> attachmentUrls,
        @Schema(description = "처리 상태", example = "RECEIVED")
        InquiryStatus status,
        @Schema(description = "관리자 답변. 없으면 null", example = "결제 내역을 확인 중입니다.")
        String answer,
        @Schema(description = "등록 시각", example = "2026-08-20T11:00:00")
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
