package com.gaguraczi.paw.domain.notification.service;

import com.gaguraczi.paw.domain.notification.entity.Notification;
import com.gaguraczi.paw.domain.notification.enums.NotificationCategory;
import com.gaguraczi.paw.domain.notification.enums.NotificationTargetType;
import com.gaguraczi.paw.domain.notification.repository.NotificationRepository;
import com.gaguraczi.paw.domain.users.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationInboxService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public Notification insert(
            UUID uid,
            NotificationCategory category,
            String title,
            String body,
            NotificationTargetType targetType,
            Long targetId,
            Long todoDateId,
            String ctaLabel
    ) {
        Notification notification = Notification.create(
                userRepository.getReferenceById(uid),
                category,
                title,
                body,
                targetType,
                targetId,
                todoDateId,
                ctaLabel
        );
        return notificationRepository.save(notification);
    }
}
