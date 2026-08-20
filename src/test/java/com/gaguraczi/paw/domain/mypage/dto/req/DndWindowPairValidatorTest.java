package com.gaguraczi.paw.domain.mypage.dto.req;

import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;

class DndWindowPairValidatorTest {

    private final DndWindowPairValidator validator = new DndWindowPairValidator();

    @Test
    void 시작과_종료를_함께_보내거나_둘_다_생략하면_통과한다() {
        NotificationSettingUpdateReq both = new NotificationSettingUpdateReq(
                null, null, null, null, null, null, null, LocalTime.of(22, 0), LocalTime.of(7, 0));
        NotificationSettingUpdateReq neither = new NotificationSettingUpdateReq(
                true, null, null, null, null, null, null, null, null);

        assertThat(validator.isValid(both, null)).isTrue();
        assertThat(validator.isValid(neither, null)).isTrue();
    }

    @Test
    void 한쪽만_있으면_실패한다() {
        NotificationSettingUpdateReq startOnly = new NotificationSettingUpdateReq(
                null, null, null, null, null, null, null, LocalTime.of(22, 0), null);

        assertThat(validator.isValid(startOnly, null)).isFalse();
    }
}
