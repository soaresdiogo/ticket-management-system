package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.UpdateTicketStatusPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketStatusHistory;
import com.di2it.ticket_service.domain.repository.TicketRepository;
import com.di2it.ticket_service.domain.repository.TicketStatusHistoryRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Adapter that persists ticket status update and appends a status history record.
 */
@Component
public class UpdateTicketStatusAdapter implements UpdateTicketStatusPort {

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;

    public UpdateTicketStatusAdapter(
        TicketRepository ticketRepository,
        TicketStatusHistoryRepository ticketStatusHistoryRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
    }

    @Override
    @Transactional
    public Ticket updateStatus(Ticket ticket, UUID changedBy, String oldStatus) {
        Ticket saved = ticketRepository.save(ticket);

        TicketStatusHistory history = TicketStatusHistory.builder()
            .ticket(saved)
            .changedBy(changedBy)
            .oldStatus(oldStatus)
            .newStatus(saved.getStatus())
            .build();
        ticketStatusHistoryRepository.save(history);

        return saved;
    }
}
