package com.di2it.notification_service.infrastructure.persistence;

import com.di2it.notification_service.application.port.FindNotificationByIdPort;
import com.di2it.notification_service.domain.entity.Notification;
import com.di2it.notification_service.domain.repository.NotificationRepository;

import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

@Component
public class FindNotificationByIdAdapter implements FindNotificationByIdPort {

    private final NotificationRepository notificationRepository;

    public FindNotificationByIdAdapter(NotificationRepository notificationRepository) {
        this.notificationRepository = notificationRepository;
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return notificationRepository.findById(id);
    }
}
