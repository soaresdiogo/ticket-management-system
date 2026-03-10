package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.application.port.SaveTicketCommentPort;
import com.di2it.ticket_service.domain.entity.TicketComment;
import com.di2it.ticket_service.domain.repository.TicketCommentRepository;

import org.springframework.stereotype.Component;

/**
 * Adapter that persists ticket comments using the repository.
 */
@Component
public class SaveTicketCommentAdapter implements SaveTicketCommentPort {

    private final TicketCommentRepository ticketCommentRepository;

    public SaveTicketCommentAdapter(TicketCommentRepository ticketCommentRepository) {
        this.ticketCommentRepository = ticketCommentRepository;
    }

    @Override
    public TicketComment save(TicketComment comment) {
        return ticketCommentRepository.save(comment);
    }
}
