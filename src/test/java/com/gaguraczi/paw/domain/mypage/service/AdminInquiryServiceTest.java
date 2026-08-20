package com.gaguraczi.paw.domain.mypage.service;

import com.gaguraczi.paw.domain.mypage.dto.req.InquiryAnswerReq;
import com.gaguraczi.paw.domain.mypage.dto.res.AdminInquiryRes;
import com.gaguraczi.paw.domain.mypage.entity.Inquiry;
import com.gaguraczi.paw.domain.mypage.enums.InquiryStatus;
import com.gaguraczi.paw.domain.mypage.enums.InquiryType;
import com.gaguraczi.paw.domain.mypage.exception.code.MypageErrorCode;
import com.gaguraczi.paw.domain.mypage.repository.InquiryRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminInquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;

    @InjectMocks
    private AdminInquiryService adminInquiryService;

    @Test
    void 목록은_타인_문의도_포함한다() {
        User writer = User.builder().uid(UUID.randomUUID()).nickname("초코엄마").email("a@b.com").build();
        Inquiry inquiry = Inquiry.builder()
                .inquiryId(7L)
                .user(writer)
                .inquiryType(InquiryType.PAYMENT)
                .content("결제가 안 돼요")
                .status(InquiryStatus.RECEIVED)
                .createdAt(LocalDateTime.of(2026, 8, 20, 11, 0))
                .build();
        when(inquiryRepository.findAllForAdmin(isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of(inquiry));

        CursorPageRes<AdminInquiryRes> res = adminInquiryService.getInquiries(null, 20, null, null);

        assertThat(res.getContent()).hasSize(1);
        assertThat(res.getContent().getFirst().uid()).isEqualTo(writer.getUid());
        assertThat(res.getContent().getFirst().nickname()).isEqualTo("초코엄마");
        assertThat(res.isHasNext()).isFalse();
    }

    @Test
    void 답변하면_ANSWERED가_된다() {
        User writer = User.builder().uid(UUID.randomUUID()).nickname("초코엄마").email("a@b.com").build();
        Inquiry inquiry = Inquiry.builder()
                .inquiryId(7L)
                .user(writer)
                .inquiryType(InquiryType.PAYMENT)
                .content("결제가 안 돼요")
                .status(InquiryStatus.RECEIVED)
                .build();
        when(inquiryRepository.findByIdWithUser(7L)).thenReturn(Optional.of(inquiry));

        AdminInquiryRes res = adminInquiryService.answer(7L, new InquiryAnswerReq("확인했습니다."));

        assertThat(res.status()).isEqualTo(InquiryStatus.ANSWERED);
        assertThat(res.answer()).isEqualTo("확인했습니다.");
        assertThat(inquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
    }

    @Test
    void 없는_문의는_404이다() {
        when(inquiryRepository.findByIdWithUser(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> adminInquiryService.getDetail(99L))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(MypageErrorCode.INQUIRY_NOT_FOUND);
    }
}
