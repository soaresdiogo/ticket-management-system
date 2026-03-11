package com.di2it.notification_service.infrastructure.persistence;

import com.di2it.notification_service.application.port.PersistEmailLogPort;
import com.di2it.notification_service.domain.entity.EmailLog;
import com.di2it.notification_service.domain.repository.EmailLogRepository;

import org.springframework.stereotype.Component;

@Component
public class EmailLogPersistenceAdapter implements PersistEmailLogPort {

    private final EmailLogRepository emailLogRepository;

    public EmailLogPersistenceAdapter(EmailLogRepository emailLogRepository) {
        this.emailLogRepository = emailLogRepository;
    }

    @Override
    public EmailLog save(EmailLog emailLog) {
        return emailLogRepository.save(emailLog);
    }
}
