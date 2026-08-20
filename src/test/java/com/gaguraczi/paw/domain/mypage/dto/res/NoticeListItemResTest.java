package com.gaguraczi.paw.domain.mypage.dto.res;

import com.gaguraczi.paw.domain.mypage.entity.Notice;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class NoticeListItemResTest {

    @Test
    void NEW_뱃지는_전달받은_오늘_날짜_기준이다() {
        LocalDate today = LocalDate.of(2026, 8, 21);

        assertThat(from(LocalDateTime.of(2026, 8, 15, 10, 0), today).isNew()).isTrue();
        assertThat(from(LocalDateTime.of(2026, 8, 14, 10, 0), today).isNew()).isFalse();
    }

    private static NoticeListItemRes from(LocalDateTime createdAt, LocalDate today) {
        Notice notice = Notice.builder().noticeId(1L).title("공지").content("본문").build();
        ReflectionTestUtils.setField(notice, "createdAt", createdAt);
        return NoticeListItemRes.from(notice, today);
    }
}
