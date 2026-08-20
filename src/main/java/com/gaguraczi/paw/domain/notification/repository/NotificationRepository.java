package com.gaguraczi.paw.domain.notification.repository;

import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    long countByUser_UidAndIsReadFalse(UUID uid);

    @Query("""
            SELECT n FROM Notification n
            WHERE n.user.uid = :uid
              AND (:#{#category == null} = true OR n.category = :category)
              AND (
                    :#{#cursorCreatedAt == null} = true
                    OR n.createdAt < :cursorCreatedAt
                    OR (n.createdAt = :cursorCreatedAt AND n.notificationId < :cursorId)
                  )
            ORDER BY n.createdAt DESC, n.notificationId DESC
            """)
    List<Notification> search(
            @Param("uid") UUID uid,
            @Param("category") NotificationCategory category,
            @Param("cursorCreatedAt") LocalDateTime cursorCreatedAt,
            @Param("cursorId") Long cursorId,
            Pageable pageable
    );

    @Modifying(clearAutomatically = true)
    @Query("""
            UPDATE Notification n
            SET n.isRead = true, n.readAt = :now
            WHERE n.user.uid = :uid AND n.isRead = false
            """)
    int markAllRead(@Param("uid") UUID uid, @Param("now") LocalDateTime now);
}
