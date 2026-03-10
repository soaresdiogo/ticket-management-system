package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.web.dto.ChangeTicketStatusResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ChangeTicketStatusResponseMapperTest {

    @Test
    @DisplayName("toResponse maps ticket to response with id, status and updatedAt")
    void toResponseMapsTicket() {
        UUID id = UUID.randomUUID();
        LocalDateTime updatedAt = LocalDateTime.now().minusHours(1);
        Ticket ticket = Ticket.builder()
            .id(id)
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("RESOLVED")
            .priority("NORMAL")
            .createdAt(LocalDateTime.now().minusDays(1))
            .updatedAt(updatedAt)
            .build();

        ChangeTicketStatusResponse response = ChangeTicketStatusResponseMapper.toResponse(ticket);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getStatus()).isEqualTo("RESOLVED");
        assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
