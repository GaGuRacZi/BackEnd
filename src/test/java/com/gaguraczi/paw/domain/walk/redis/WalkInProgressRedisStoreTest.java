package com.gaguraczi.paw.domain.walk.redis;

import com.gaguraczi.paw.domain.walk.exception.WalkErrorCode;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.utils.RedisUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalkInProgressRedisStoreTest {

    @Mock
    private RedisUtil redisUtil;

    private WalkInProgressRedisStore store;

    @BeforeEach
    void setUp() {
        store = new WalkInProgressRedisStore(redisUtil, JsonMapper.shared());
    }

    @Test
    void saveIfAbsent_LocalDateTime을_ISO문자열로_저장한다() {
        WalkInProgressSession session = WalkInProgressSession.builder()
                .petId(22L)
                .startTime(LocalDateTime.of(2026, 8, 21, 6, 38))
                .walkDate(LocalDate.of(2026, 8, 21))
                .build();
        when(redisUtil.setIfAbsent(anyString(), anyString(), anyLong())).thenReturn(true);

        assertThat(store.saveIfAbsent(session)).isTrue();

        ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
        verify(redisUtil).setIfAbsent(eq("walk:in-progress:22"), jsonCaptor.capture(), eq(6L * 60 * 60));
        String json = jsonCaptor.getValue();
        assertThat(json).contains("2026-08-21T06:38:00");
        assertThat(json).contains("\"petId\":22");

        WalkInProgressSession parsed = store.parse(json);
        assertThat(parsed.getPetId()).isEqualTo(22L);
        assertThat(parsed.getStartTime()).isEqualTo(LocalDateTime.of(2026, 8, 21, 6, 38));
        assertThat(parsed.getWalkDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(parsed.isProcessing()).isFalse();
    }

    @Test
    void getAndRefreshTtl_세션이_없으면_비어있다() {
        when(redisUtil.getData("walk:in-progress:22")).thenReturn(null);

        Optional<WalkInProgressSession> result = store.getAndRefreshTtl(22L);

        assertThat(result).isEmpty();
    }

    @Test
    void parse_잘못된_JSON은_WALK_404_3이_아니라_WALK_500_1이다() {
        assertThatThrownBy(() -> store.parse("not-json"))
                .isInstanceOf(GeneralException.class)
                .extracting(ex -> ((GeneralException) ex).getCode())
                .isEqualTo(WalkErrorCode.WALK_SESSION_CORRUPT);
    }
}
