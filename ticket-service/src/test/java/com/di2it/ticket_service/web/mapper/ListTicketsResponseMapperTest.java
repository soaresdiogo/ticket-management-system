package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.ListTicketsResponse;
import com.di2it.ticket_service.web.dto.TicketListItemResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ListTicketsResponseMapperTest {

    @Test
    @DisplayName("maps page of tickets to ListTicketsResponse with content and metadata")
    void toResponse_mapsPageToResponse() {
        UUID id = UUID.randomUUID();
        UUID tenantId = UUID.randomUUID();
        UUID clientId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        Ticket ticket = Ticket.builder()
            .id(id)
            .tenantId(tenantId)
            .clientId(clientId)
            .title("Title")
            .description("Description")
            .status("OPEN")
            .priority("HIGH")
            .category("Billing")
            .createdAt(createdAt)
            .updatedAt(LocalDateTime.now())
            .build();
        Page<Ticket> page = new PageImpl<>(List.of(ticket), PageRequest.of(0, 20), 1);

        ListTicketsResponse response = ListTicketsResponseMapper.toResponse(page);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        TicketListItemResponse item = response.getContent().get(0);
        assertThat(item.getId()).isEqualTo(id);
        assertThat(item.getTenantId()).isEqualTo(tenantId);
        assertThat(item.getClientId()).isEqualTo(clientId);
        assertThat(item.getTitle()).isEqualTo("Title");
        assertThat(item.getStatus()).isEqualTo("OPEN");
        assertThat(response.getTotalElements()).isEqualTo(1);
        assertThat(response.getTotalPages()).isEqualTo(1);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
    }

    @Test
    @DisplayName("maps empty page to ListTicketsResponse with empty content")
    void toResponse_mapsEmptyPage() {
        Page<Ticket> emptyPage = new PageImpl<>(List.of(), PageRequest.of(0, 20), 0);

        ListTicketsResponse response = ListTicketsResponseMapper.toResponse(emptyPage);

        assertThat(response).isNotNull();
        assertThat(response.getContent()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
        assertThat(response.getTotalPages()).isEqualTo(0);
        assertThat(response.getNumber()).isEqualTo(0);
        assertThat(response.getSize()).isEqualTo(20);
    }
}
