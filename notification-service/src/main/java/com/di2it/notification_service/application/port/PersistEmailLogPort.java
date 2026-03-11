package com.di2it.notification_service.application.port;

import com.di2it.notification_service.domain.entity.EmailLog;

/**
 * Port for persisting email send audit log.
 */
@FunctionalInterface
public interface PersistEmailLogPort {

    EmailLog save(EmailLog emailLog);
}
