package com.di2it.ticket_service.web.mapper;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketComment;
import com.di2it.ticket_service.web.dto.AddCommentResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AddCommentResponseMapperTest {

    @Test
    @DisplayName("toResponse maps comment to response with all fields")
    void toResponseMapsComment() {
        UUID id = UUID.randomUUID();
        UUID authorId = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now().minusHours(1);
        Ticket ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("OPEN")
            .build();
        TicketComment comment = TicketComment.builder()
            .id(id)
            .ticket(ticket)
            .authorId(authorId)
            .authorRole("ACCOUNTANT")
            .content("Internal note")
            .internal(true)
            .createdAt(createdAt)
            .build();

        AddCommentResponse response = AddCommentResponseMapper.toResponse(comment);

        assertThat(response.getId()).isEqualTo(id);
        assertThat(response.getContent()).isEqualTo("Internal note");
        assertThat(response.getAuthorId()).isEqualTo(authorId);
        assertThat(response.getAuthorRole()).isEqualTo("ACCOUNTANT");
        assertThat(response.isInternal()).isTrue();
        assertThat(response.getCreatedAt()).isEqualTo(createdAt);
    }
}
