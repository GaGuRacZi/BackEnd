package com.gaguraczi.paw.domain.notification.entity;

import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.users.entity.User;
import com.gaguraczi.paw.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "notification",
        indexes = {
                @Index(name = "idx_notification_uid_created", columnList = "uid, created_at, notification_id"),
                @Index(name = "idx_notification_uid_category", columnList = "uid, category, created_at")
        }
)
public class Notification extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "uid", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private NotificationCategory category;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "body", nullable = false, length = 500)
    private String body;

    @Column(name = "is_read", nullable = false)
    private boolean isRead;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "target_type", nullable = false, length = 20)
    private NotificationTargetType targetType;

    @Column(name = "target_id")
    private Long targetId;

    @Column(name = "todo_date_id")
    private Long todoDateId;

    @Column(name = "cta_label", nullable = false, length = 40)
    private String ctaLabel;

    public static Notification create(
            User user,
            NotificationCategory category,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            Long todoDateId,
            String ctaLabel
    ) {
        Notification notification = new Notification();
        notification.user = user;
        notification.category = category;
        notification.title = clip(title, 200);
        notification.body = clip(body, 500);
        notification.isRead = false;
        notification.readAt = null;
        notification.targetType = targetType;
        notification.targetId = targetId;
        notification.todoDateId = todoDateId;
        notification.ctaLabel = ctaLabel;
        return notification;
    }

    public void markRead(LocalDateTime now) {
        if (this.isRead) {
            return;
        }
        this.isRead = true;
        this.readAt = now;
    }

    public boolean isOwnedBy(java.util.UUID uid) {
        return this.user.getUid().equals(uid);
    }

    private static String clip(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max);
    }
}
