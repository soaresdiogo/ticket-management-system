package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusResponse;

/**
 * Maps Ticket entity to ChangeTicketStatusResponse for PATCH /tickets/{id}/status.
 */
public final class ChangeTicketStatusResponseMapper {

    private ChangeTicketStatusResponseMapper() {
    }

    public static ChangeTicketStatusResponse toResponse(Ticket ticket) {
        return ChangeTicketStatusResponse.builder()
            .id(ticket.getId())
            .status(ticket.getStatus())
            .updatedAt(ticket.getUpdatedAt())
            .build();
    }
}
