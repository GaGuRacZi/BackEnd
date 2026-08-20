package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import com.gaguraczi.paw.domain.users.entity.User;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Schema(description = "관리자용 문의. 작성자 정보를 포함합니다.")
public record AdminInquiryRes(
        @Schema(description = "문의 ID", example = "1")
        Long inquiryId,
        @Schema(description = "작성자 uid", example = "550e8400-e29b-41d4-a716-446655440000")
        UUID uid,
        @Schema(description = "작성자 닉네임", example = "길동이")
        String nickname,
        @Schema(description = "작성자 이메일", example = "user@example.com")
        String email,
        @Schema(description = "문의 유형", example = "PAYMENT")
        InquiryType inquiryType,
        @Schema(description = "문의 내용", example = "구독 결제가 반복해서 실패해요.")
        String content,
        @Schema(description = "첨부 파일 URL 목록")
        List<String> attachmentUrls,
        @Schema(description = "처리 상태", example = "RECEIVED")
        InquiryStatus status,
        @Schema(description = "관리자 답변. 없으면 null", example = "결제 내역을 확인했습니다. 재시도해 주세요.")
        String answer,
        @Schema(description = "등록 시각", example = "2026-08-20T11:00:00")
        LocalDateTime createdAt
) {
    public static AdminInquiryRes from(Inquiry inquiry) {
        User user = inquiry.getUser();
        return new AdminInquiryRes(
                inquiry.getInquiryId(),
                user.getUid(),
                user.getNickname(),
                user.getEmail(),
                inquiry.getInquiryType(),
                inquiry.getContent(),
                inquiry.getAttachmentUrls(),
                inquiry.getStatus(),
                inquiry.getAnswer(),
                inquiry.getCreatedAt()
        );
    }
}
