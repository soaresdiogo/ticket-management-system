package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;

/**
 * Port for publishing ticket status changed events (e.g. to Kafka).
 */
@FunctionalInterface
public interface PublishTicketStatusChangedPort {

    /**
     * Publishes the status change event to the messaging infrastructure.
     *
     * @param event the event payload (ticketId, userId, oldStatus, newStatus, timestamp)
     */
    void publish(TicketStatusChangedEvent event);
}
