package com.di2it.ticket_service.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketStatusHistoryEntityTest {

    @Test
    @DisplayName("builder sets all fields correctly")
    void builderSetsFields() {
        UUID id = UUID.randomUUID();
        UUID changedBy = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("T").description("D").status("OPEN")
            .build();

        TicketStatusHistory history = TicketStatusHistory.builder()
            .id(id)
            .ticket(ticket)
            .changedBy(changedBy)
            .oldStatus("OPEN")
            .newStatus("IN_PROGRESS")
            .comment("Started work")
            .build();

        assertThat(history.getId()).isEqualTo(id);
        assertThat(history.getTicket()).isEqualTo(ticket);
        assertThat(history.getChangedBy()).isEqualTo(changedBy);
        assertThat(history.getOldStatus()).isEqualTo("OPEN");
        assertThat(history.getNewStatus()).isEqualTo("IN_PROGRESS");
        assertThat(history.getComment()).isEqualTo("Started work");
    }
}
