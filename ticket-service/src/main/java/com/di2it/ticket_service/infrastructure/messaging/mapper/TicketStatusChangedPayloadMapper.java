package com.di2it.ticket_service.infrastructure.messaging.mapper;

import com.di2it.ticket_service.application.event.TicketStatusChangedEvent;
import com.di2it.ticket_service.infrastructure.messaging.payload.TicketStatusChangedPayload;

import org.springframework.stereotype.Component;

/**
 * Maps domain event to Kafka wire payload. Keeps serialization contract in infrastructure.
 */
@Component
public class TicketStatusChangedPayloadMapper {

    public TicketStatusChangedPayload toPayload(TicketStatusChangedEvent event) {
        if (event == null) {
            return null;
        }
        return new TicketStatusChangedPayload(
            TicketStatusChangedPayload.EVENT_TYPE,
            TicketStatusChangedPayload.EVENT_VERSION,
            event.getTicketId(),
            event.getTenantId(),
            event.getUserId(),
            event.getClientId(),
            event.getOldStatus(),
            event.getNewStatus(),
            event.getTimestamp()
        );
    }
}
