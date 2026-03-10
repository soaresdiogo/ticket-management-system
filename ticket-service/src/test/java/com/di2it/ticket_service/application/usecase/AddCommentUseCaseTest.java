package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.FindTicketByIdAndTenantIdPort;
import com.di2it.ticket_service.application.port.SaveTicketCommentPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketComment;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AddCommentUseCaseTest {

    @Mock
    private FindTicketByIdAndTenantIdPort findTicketPort;

    @Mock
    private SaveTicketCommentPort saveCommentPort;

    @InjectMocks
    private AddCommentUseCase addCommentUseCase;

    private UUID ticketId;
    private UUID tenantId;
    private UUID authorId;
    private Ticket ticket;
    private AddCommentCommand command;
    private TicketComment savedComment;

    @BeforeEach
    void setUp() {
        ticketId = UUID.randomUUID();
        tenantId = UUID.randomUUID();
        authorId = UUID.randomUUID();
        ticket = Ticket.builder()
            .id(ticketId)
            .tenantId(tenantId)
            .clientId(UUID.randomUUID())
            .title("Title")
            .description("Desc")
            .status("OPEN")
            .priority("NORMAL")
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();
        command = AddCommentCommand.builder()
            .ticketId(ticketId)
            .tenantId(tenantId)
            .authorId(authorId)
            .authorRole("ACCOUNTANT")
            .content("Internal note here")
            .internal(true)
            .build();
        savedComment = TicketComment.builder()
            .id(UUID.randomUUID())
            .ticket(ticket)
            .authorId(authorId)
            .authorRole("ACCOUNTANT")
            .content("Internal note here")
            .internal(true)
            .createdAt(LocalDateTime.now())
            .build();
    }

    @Nested
    @DisplayName("addComment")
    class AddComment {

        @Test
        @DisplayName("returns empty when ticket not found")
        void returnsEmptyWhenTicketNotFound() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.empty());

            Optional<TicketComment> result = addCommentUseCase.addComment(command);

            assertThat(result).isEmpty();
            verify(saveCommentPort, never()).save(any());
        }

        @Test
        @DisplayName("saves comment and returns it when ticket exists")
        void savesAndReturnsCommentWhenTicketExists() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            when(saveCommentPort.save(any(TicketComment.class))).thenReturn(savedComment);

            Optional<TicketComment> result = addCommentUseCase.addComment(command);

            assertThat(result).containsSame(savedComment);
            ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
            verify(saveCommentPort).save(captor.capture());
            TicketComment captured = captor.getValue();
            assertThat(captured.getTicket()).isSameAs(ticket);
            assertThat(captured.getAuthorId()).isEqualTo(authorId);
            assertThat(captured.getAuthorRole()).isEqualTo("ACCOUNTANT");
            assertThat(captured.getContent()).isEqualTo("Internal note here");
            assertThat(captured.isInternal()).isTrue();
        }

        @Test
        @DisplayName("trims content before saving")
        void trimsContent() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            when(saveCommentPort.save(any(TicketComment.class))).thenReturn(savedComment);
            command = AddCommentCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .authorId(authorId)
                .authorRole("CLIENT")
                .content("  Some content  ")
                .internal(false)
                .build();

            addCommentUseCase.addComment(command);

            ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
            verify(saveCommentPort).save(captor.capture());
            assertThat(captor.getValue().getContent()).isEqualTo("Some content");
        }

        @Test
        @DisplayName("uses default author role when role is null")
        void usesDefaultAuthorRoleWhenNull() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            when(saveCommentPort.save(any(TicketComment.class))).thenReturn(savedComment);
            command = AddCommentCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .authorId(authorId)
                .authorRole(null)
                .content("Comment")
                .internal(false)
                .build();

            addCommentUseCase.addComment(command);

            ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
            verify(saveCommentPort).save(captor.capture());
            assertThat(captor.getValue().getAuthorRole()).isEqualTo("CLIENT");
        }

        @Test
        @DisplayName("uses default author role when role is blank")
        void usesDefaultAuthorRoleWhenBlank() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            when(saveCommentPort.save(any(TicketComment.class))).thenReturn(savedComment);
            command = AddCommentCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .authorId(authorId)
                .authorRole("   ")
                .content("Comment")
                .internal(false)
                .build();

            addCommentUseCase.addComment(command);

            ArgumentCaptor<TicketComment> captor = ArgumentCaptor.forClass(TicketComment.class);
            verify(saveCommentPort).save(captor.capture());
            assertThat(captor.getValue().getAuthorRole()).isEqualTo("CLIENT");
        }

        @Test
        @DisplayName("throws when content is null")
        void throwsWhenContentNull() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            command = AddCommentCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .authorId(authorId)
                .authorRole("CLIENT")
                .content(null)
                .internal(false)
                .build();

            assertThatThrownBy(() -> addCommentUseCase.addComment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment content cannot be null or blank");
            verify(saveCommentPort, never()).save(any());
        }

        @Test
        @DisplayName("throws when content is blank")
        void throwsWhenContentBlank() {
            when(findTicketPort.findByIdAndTenantId(ticketId, tenantId)).thenReturn(Optional.of(ticket));
            command = AddCommentCommand.builder()
                .ticketId(ticketId)
                .tenantId(tenantId)
                .authorId(authorId)
                .authorRole("CLIENT")
                .content("   ")
                .internal(false)
                .build();

            assertThatThrownBy(() -> addCommentUseCase.addComment(command))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Comment content cannot be null or blank");
            verify(saveCommentPort, never()).save(any());
        }
    }
}
