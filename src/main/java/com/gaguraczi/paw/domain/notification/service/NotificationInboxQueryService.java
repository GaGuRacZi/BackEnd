package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.notification.dto.res.NotificationItemRes;
import com.gaguraczi.paw.domain.notification.dto.res.NotificationReadAllRes;
import com.gaguraczi.paw.domain.notification.dto.res.NotificationUnreadCountRes;
import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.exception.code.NotificationErrorCode;
import com.gaguraczi.paw.domain.notification.repository.NotificationRepository;
import com.gaguraczi.paw.domain.notification.support.NotificationCursorCodec;
import com.gaguraczi.paw.global.api.CursorPageRes;
import com.gaguraczi.paw.global.exception.GeneralException;
import com.gaguraczi.paw.global.security.SecurityUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationInboxQueryService {

    private static final int DEFAULT_SIZE = 20;
    private static final int MAX_SIZE = 50;

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;
    private final Clock clock;

    public CursorPageRes<NotificationItemRes> list(NotificationCategory category, String cursor, Integer size) {
        UUID uid = securityUtils.currentUid();
        int pageSize = normalizeSize(size);
        NotificationCursorCodec.Cursor decoded = NotificationCursorCodec.decode(cursor);
        List<Notification> rows = notificationRepository.search(
                uid,
                category,
                decoded == null ? null : decoded.createdAt(),
                decoded == null ? null : decoded.id(),
                PageRequest.of(0, pageSize + 1)
        );
        boolean hasNext = rows.size() > pageSize;
        List<Notification> page = hasNext ? rows.subList(0, pageSize) : rows;
        List<NotificationItemRes> content = page.stream().map(NotificationItemRes::from).toList();
        String nextCursor = hasNext && !page.isEmpty()
                ? NotificationCursorCodec.encode(page.getLast().getCreatedAt(), page.getLast().getNotificationId())
                : null;
        return CursorPageRes.of(content, nextCursor, hasNext, pageSize);
    }

    public NotificationUnreadCountRes unreadCount() {
        return new NotificationUnreadCountRes(
                notificationRepository.countByUser_UidAndIsReadFalse(securityUtils.currentUid())
        );
    }

    @Transactional
    public NotificationItemRes markRead(Long notificationId) {
        UUID uid = securityUtils.currentUid();
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> GeneralException.of(NotificationErrorCode.NOTIFICATION_NOT_FOUND));
        if (!notification.isOwnedBy(uid)) {
            throw GeneralException.of(NotificationErrorCode.NOTIFICATION_NOT_FOUND);
        }
        notification.markRead(LocalDateTime.now(clock));
        return NotificationItemRes.from(notification);
    }

    @Transactional
    public NotificationReadAllRes markAllRead() {
        UUID uid = securityUtils.currentUid();
        int updated = notificationRepository.markAllRead(uid, LocalDateTime.now(clock));
        return new NotificationReadAllRes(updated);
    }

    private int normalizeSize(Integer size) {
        if (size == null || size < 1) {
            return DEFAULT_SIZE;
        }
        return Math.min(size, MAX_SIZE);
    }
}
