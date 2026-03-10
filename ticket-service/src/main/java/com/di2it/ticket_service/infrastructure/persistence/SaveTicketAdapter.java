package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.CreateTicketPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketStatusHistory;
import com.di2it.ticket_service.domain.repository.TicketRepository;
import com.di2it.ticket_service.domain.repository.TicketStatusHistoryRepository;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * Adapter that persists a new ticket and its initial status history in one transaction.
 */
@Component
public class SaveTicketAdapter implements CreateTicketPort {

    private static final String INITIAL_STATUS = "OPEN";
    private static final String STATUS_ORIGIN = "NONE";

    private final TicketRepository ticketRepository;
    private final TicketStatusHistoryRepository ticketStatusHistoryRepository;

    public SaveTicketAdapter(
        TicketRepository ticketRepository,
        TicketStatusHistoryRepository ticketStatusHistoryRepository
    ) {
        this.ticketRepository = ticketRepository;
        this.ticketStatusHistoryRepository = ticketStatusHistoryRepository;
    }

    @Override
    @Transactional
    public Ticket save(Ticket ticket, UUID changedBy) {
        Ticket saved = ticketRepository.save(ticket);

        TicketStatusHistory initialHistory = TicketStatusHistory.builder()
            .ticket(saved)
            .changedBy(changedBy)
            .oldStatus(STATUS_ORIGIN)
            .newStatus(INITIAL_STATUS)
            .build();

        ticketStatusHistoryRepository.save(initialHistory);

        return saved;
    }
}
