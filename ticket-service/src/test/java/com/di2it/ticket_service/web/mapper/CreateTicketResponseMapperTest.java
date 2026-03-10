package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.CreateTicketResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CreateTicketResponseMapperTest {

    @Test
    @DisplayName("maps ticket entity to CreateTicketResponse")
    void toResponse_mapsAllFields() {
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

        CreateTicketResponse response = CreateTicketResponseMapper.toResponse(ticket);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getTenantId()).isEqualTo(tenantId);
        assertThat(response.getClientId()).isEqualTo(clientId);
        assertThat(response.getTitle()).isEqualTo("Title");
        assertThat(response.getDescription()).isEqualTo("Description");
        assertThat(response.getStatus()).isEqualTo("OPEN");
        assertThat(response.getPriority()).isEqualTo("HIGH");
        assertThat(response.getCategory()).isEqualTo("Billing");
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
