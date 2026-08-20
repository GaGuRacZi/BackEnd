package com.gaguraczi.paw.domain.notification.dto.res;

import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDateTime;

public record NotificationItemRes(
        Long id,
        NotificationCategory category,
        String title,
        String body,
        @JsonProperty("isRead") boolean isRead,
        LocalDateTime createdAt,
        String ctaLabel,
        NotificationTargetType targetType,
        Long targetId
) {
    public static NotificationItemRes from(Notification notification) {
        return new NotificationItemRes(
                notification.getNotificationId(),
                notification.getCategory(),
                notification.getTitle(),
                notification.getBody(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getCtaLabel(),
                notification.getTargetType(),
                notification.getTargetId()
        );
    }
}
