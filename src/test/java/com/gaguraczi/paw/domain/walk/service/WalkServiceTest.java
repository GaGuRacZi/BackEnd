package com.gaguraczi.paw.domain.walk.service;

import com.gaguraczi.paw.domain.users.entity.Pet;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.domain.users.repository.PetRepository;
import com.gaguraczi.paw.domain.walk.dto.request.WalkStartRequest;
import com.gaguraczi.paw.domain.walk.dto.response.WalkStartResponse;
import com.gaguraczi.paw.domain.walk.dto.response.WalkWeeklySummaryResponse;
import com.gaguraczi.paw.domain.walk.enums.WalkStatusEnum;
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressRedisStore;
import com.gaguraczi.paw.domain.walk.redis.WalkInProgressSession;
import com.gaguraczi.paw.domain.walk.repository.WalkRepository;
import com.gaguraczi.paw.global.security.SecurityUtils;
import com.gaguraczi.paw.global.time.AppTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WalkServiceTest {

    @Mock
    private WalkRepository walkRepository;
    @Mock
    private PetRepository petRepository;
    @Mock
    private WalkInProgressRedisStore walkInProgressRedisStore;
    @Mock
    private SecurityUtils securityUtils;

    private User user;
    private Pet pet;

    @BeforeEach
    void setUp() {
        user = User.builder().uid(UUID.randomUUID()).build();
        pet = Pet.builder().petId(1L).user(user).petName("초코").build();
        when(securityUtils.currentUser()).thenReturn(user);
        when(petRepository.findById(1L)).thenReturn(Optional.of(pet));
    }

    @Test
    void 시작시각을_생략하면_KST_벽시계와_그_날짜를_쓴다() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-20T20:00:00Z"), AppTime.KST);
        WalkService walkService = walkService(clock);
        when(walkInProgressRedisStore.saveIfAbsent(any())).thenReturn(true);

        WalkStartResponse res = walkService.startWalk(WalkStartRequest.builder().petId(1L).build());

        assertThat(res.getStartTime()).isEqualTo(LocalDateTime.of(2026, 8, 21, 5, 0));
        assertThat(res.getWalkDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(res.getWalkStatus()).isEqualTo(WalkStatusEnum.IN_PROGRESS);

        ArgumentCaptor<WalkInProgressSession> captor = ArgumentCaptor.forClass(WalkInProgressSession.class);
        verify(walkInProgressRedisStore).saveIfAbsent(captor.capture());
        assertThat(captor.getValue().getWalkDate()).isEqualTo(LocalDate.of(2026, 8, 21));
        assertThat(captor.getValue().getStartTime()).isEqualTo(LocalDateTime.of(2026, 8, 21, 5, 0));
    }

    @Test
    void 주간요약_기본일은_UTC_일요일이_아니라_KST_월요일이다() {
        Clock clock = Clock.fixed(Instant.parse("2026-08-16T20:00:00Z"), AppTime.KST);
        WalkService walkService = walkService(clock);
        when(walkRepository.findAllByPet_PetIdAndWalkDateBetweenOrderByWalkDateDescStartTimeDesc(
                any(), any(), any()
        )).thenReturn(List.of());

        WalkWeeklySummaryResponse res = walkService.getWeeklySummary(1L, null);

        assertThat(res.getWeekStartDate()).isEqualTo(LocalDate.of(2026, 8, 17));
        assertThat(res.getWeekEndDate()).isEqualTo(LocalDate.of(2026, 8, 23));
    }

    private WalkService walkService(Clock clock) {
        return new WalkService(
                walkRepository,
                petRepository,
                walkInProgressRedisStore,
                securityUtils,
                clock
        );
    }
}
