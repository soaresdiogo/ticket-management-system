package com.di2it.ticket_service.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TicketCommentEntityTest {

    @Test
    @DisplayName("builder sets all fields with internal default false")
    void builderSetsFields() {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        Ticket ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("T").description("D").status("OPEN")
            .build();

        TicketComment comment = TicketComment.builder()
            .id(id)
            .ticket(ticket)
            .authorId(authorId)
            .authorRole("CLIENT")
            .content("Comment text")
            .build();

        assertThat(comment.getId()).isEqualTo(id);
        assertThat(comment.getTicket()).isEqualTo(ticket);
        assertThat(comment.getAuthorId()).isEqualTo(authorId);
        assertThat(comment.getAuthorRole()).isEqualTo("CLIENT");
        assertThat(comment.getContent()).isEqualTo("Comment text");
        assertThat(comment.isInternal()).isFalse();
    }

    @Test
    @DisplayName("builder sets internal true when specified")
    void builderSetsInternalTrue() {
        Ticket ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("T").description("D").status("OPEN")
            .build();

        TicketComment comment = TicketComment.builder()
            .ticket(ticket)
            .authorId(UUID.randomUUID())
            .authorRole("ACCOUNTANT")
            .content("Internal note")
            .internal(true)
            .build();

        assertThat(comment.isInternal()).isTrue();
    }
}
