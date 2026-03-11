package com.di2it.notification_service.infrastructure.persistence;

import com.di2it.notification_service.application.port.PersistNotificationPort;
import com.di2it.notification_service.domain.entity.Notification;
import com.di2it.notification_service.domain.repository.NotificationRepository;

import org.springframework.stereotype.Component;

@Component
public class NotificationPersistenceAdapter implements PersistNotificationPort {

    private final NotificationRepository notificationRepository;

    public NotificationPersistenceAdapter(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return notificationRepository.save(notification);
    }
}
