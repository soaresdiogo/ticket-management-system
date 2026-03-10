package com.di2it.ticket_service.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketEntityTest {

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("builds ticket with required fields and default priority")
        void buildsWithDefaults() {
            UUID tenantId = UUID.randomUUID();
            UUID clientId = UUID.randomUUID();
            Ticket ticket = Ticket.builder()
                .tenantId(tenantId)
                .clientId(clientId)
                .title("Title")
                .description("Description")
                .status("OPEN")
                .build();

            assertThat(ticket.getTenantId()).isEqualTo(tenantId);
            assertThat(ticket.getClientId()).isEqualTo(clientId);
            assertThat(ticket.getTitle()).isEqualTo("Title");
            assertThat(ticket.getDescription()).isEqualTo("Description");
            assertThat(ticket.getStatus()).isEqualTo("OPEN");
            assertThat(ticket.getPriority()).isEqualTo("NORMAL");
            assertThat(ticket.getId()).isNull();
            assertThat(ticket.getCreatedAt()).isNull();
            assertThat(ticket.getUpdatedAt()).isNull();
        }

        @Test
        @DisplayName("builds ticket with all optional fields")
        void buildsWithOptionals() {
            UUID id = UUID.randomUUID();
            UUID accountantId = UUID.randomUUID();
            LocalDateTime resolvedAt = LocalDateTime.now().minusDays(1);
            Ticket ticket = Ticket.builder()
                .id(id)
                .tenantId(UUID.randomUUID())
                .clientId(UUID.randomUUID())
                .accountantId(accountantId)
                .title("Title")
                .description("Desc")
                .status("RESOLVED")
                .priority("HIGH")
                .category("BILLING")
                .resolvedAt(resolvedAt)
                .build();

            assertThat(ticket.getId()).isEqualTo(id);
            assertThat(ticket.getAccountantId()).isEqualTo(accountantId);
            assertThat(ticket.getPriority()).isEqualTo("HIGH");
            assertThat(ticket.getCategory()).isEqualTo("BILLING");
            assertThat(ticket.getResolvedAt()).isEqualTo(resolvedAt);
        }
    }

    @Nested
    @DisplayName("equals and hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("uses only id for equality")
        void usesIdOnly() {
            UUID id = UUID.randomUUID();
            Ticket a = Ticket.builder().id(id).tenantId(UUID.randomUUID()).clientId(UUID.randomUUID())
                .title("A").description("D").status("OPEN").build();
            Ticket b = Ticket.builder().id(id).tenantId(UUID.randomUUID()).clientId(UUID.randomUUID())
                .title("B").description("D2").status("CLOSED").build();

            assertThat(a).isEqualTo(b);
            assertThat(a.hashCode()).isEqualTo(b.hashCode());
        }
    }
}
