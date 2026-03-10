package com.di2it.ticket_service.infrastructure.persistence;

import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketComment;
import com.di2it.ticket_service.domain.repository.TicketCommentRepository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SaveTicketCommentAdapterTest {

    @Mock
    private TicketCommentRepository ticketCommentRepository;

    @InjectMocks
    private SaveTicketCommentAdapter adapter;

    @Test
    @DisplayName("save delegates to repository and returns persisted comment")
    void saveDelegatesToRepository() {
        Ticket ticket = Ticket.builder()
            .id(UUID.randomUUID())
            .tenantId(UUID.randomUUID())
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("OPEN")
            .build();
        TicketComment comment = TicketComment.builder()
            .ticket(ticket)
            .authorId(UUID.randomUUID())
            .authorRole("CLIENT")
            .content("A comment")
            .internal(false)
            .build();
        TicketComment saved = TicketComment.builder()
            .id(UUID.randomUUID())
            .ticket(ticket)
            .authorId(comment.getAuthorId())
            .authorRole(comment.getAuthorRole())
            .content(comment.getContent())
            .internal(comment.isInternal())
            .createdAt(LocalDateTime.now())
            .build();
        when(ticketCommentRepository.save(any(TicketComment.class))).thenReturn(saved);

        TicketComment result = adapter.save(comment);

        assertThat(result).isSameAs(saved);
        verify(ticketCommentRepository).save(comment);
    }
}
