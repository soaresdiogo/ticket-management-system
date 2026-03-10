package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.ListTicketsResponse;
import com.di2it.ticket_service.web.dto.TicketListItemResponse;

import org.springframework.data.domain.Page;

/**
 * Maps a Page of Ticket entities to ListTicketsResponse DTO.
 */
public final class ListTicketsResponseMapper {

    private ListTicketsResponseMapper() {
    }

    public static ListTicketsResponse toResponse(Page<Ticket> page) {
        return ListTicketsResponse.builder()
            .content(page.getContent().stream()
                .map(ListTicketsResponseMapper::toItem)
                .toList())
            .totalElements(page.getTotalElements())
            .totalPages(page.getTotalPages())
            .number(page.getNumber())
            .size(page.getSize())
            .build();
    }

    private static TicketListItemResponse toItem(Ticket ticket) {
        return TicketListItemResponse.builder()
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
