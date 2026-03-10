package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;

/**
 * Maps Ticket entity to CreateTicketResponse DTO.
 */
public final class CreateTicketResponseMapper {

    private CreateTicketResponseMapper() {
    }

    public static CreateTicketResponse toResponse(Ticket ticket) {
        return CreateTicketResponse.builder()
            .id(ticket.getId())
            .tenantId(ticket.getTenantId())
            .clientId(ticket.getClientId())
            .title(ticket.getTitle())
            .description(ticket.getDescription())
            .status(ticket.getStatus())
            .priority(ticket.getPriority())
            .category(ticket.getCategory())
            .createdAt(ticket.getCreatedAt())
            .build();
    }
}
