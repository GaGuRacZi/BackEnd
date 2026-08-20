package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.notification.dto.res.NotificationItemRes;
import com.gaguraczi.paw.domain.notification.dto.res.NotificationReadAllRes;
import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.exception.code.NotificationErrorCode;
import com.gaguraczi.paw.domain.notification.repository.NotificationRepository;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class NotificationInboxQueryServiceTest {

    @Mock
    private NotificationRepository notificationRepository;
    @Mock
    private SecurityUtils securityUtils;

    private NotificationInboxQueryService service;
    private final UUID uid = UUID.randomUUID();
    private final User user = User.builder().uid(uid).build();
    private final Clock clock = Clock.fixed(Instant.parse("2026-08-19T12:00:00Z"), ZoneId.of("Asia/Seoul"));

    @BeforeEach
    void setUp() {
        service = new NotificationInboxQueryService(notificationRepository, securityUtils, clock);
        when(securityUtils.currentUid()).thenReturn(uid);
    }

    @Test
    void 카테고리_필터로_목록을_조회한다() {
        Notification row = Notification.create(
                user, NotificationCategory.TODO, "심장약 복용 체크가 필요해요",
                "오늘 20:00 · 미완료 상태예요", NotificationTargetType.TODO, 1L, 9L, "할 일 보기"
        );
        when(notificationRepository.search(eq(uid), eq(NotificationCategory.TODO), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(List.of(row));

        CursorPageRes<NotificationItemRes> page = service.list(NotificationCategory.TODO, null, 20);

        assertThat(page.getContent()).hasSize(1);
        assertThat(page.getContent().getFirst().category()).isEqualTo(NotificationCategory.TODO);
        assertThat(page.getContent().getFirst().ctaLabel()).isEqualTo("할 일 보기");
        assertThat(page.isHasNext()).isFalse();
    }

    @Test
    void 모두_읽음은_미읽음_건수를_갱신한다() {
        when(notificationRepository.markAllRead(eq(uid), any(LocalDateTime.class))).thenReturn(3);

        NotificationReadAllRes res = service.markAllRead();

        assertThat(res.updatedCount()).isEqualTo(3);
        verify(notificationRepository).markAllRead(eq(uid), any(LocalDateTime.class));
    }

    @Test
    void 다른_유저_알림은_읽음_처리할_수_없다() {
        User other = User.builder().uid(UUID.randomUUID()).build();
        Notification row = Notification.create(
                other, NotificationCategory.AI, "t", "b",
                NotificationTargetType.VISIT, 1L, null, "요약 보기"
        );
        when(notificationRepository.findById(1L)).thenReturn(Optional.of(row));

        assertThatThrownBy(() -> service.markRead(1L))
                .isInstanceOf(GeneralException.class)
                .extracting(e -> ((GeneralException) e).getCode())
                .isEqualTo(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
