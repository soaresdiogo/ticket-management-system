package com.di2it.ticket_service.application.usecase;

import com.di2it.ticket_service.application.port.FindTicketByIdAndTenantIdPort;
import com.di2it.ticket_service.application.port.SaveTicketCommentPort;
import com.di2it.ticket_service.domain.entity.Ticket;
import com.di2it.ticket_service.domain.entity.TicketComment;

import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Use case: add a comment to a ticket. Ensures the ticket exists and belongs to the tenant.
 */
@Service
public class AddCommentUseCase {

    private static final String DEFAULT_AUTHOR_ROLE = "CLIENT";

    private final FindTicketByIdAndTenantIdPort findTicketPort;
    private final SaveTicketCommentPort saveCommentPort;

    public AddCommentUseCase(
        FindTicketByIdAndTenantIdPort findTicketPort,
        SaveTicketCommentPort saveCommentPort
    ) {
        this.findTicketPort = findTicketPort;
        this.saveCommentPort = saveCommentPort;
    }

    /**
     * Adds a comment to the ticket if it exists and belongs to the tenant.
     *
     * @param command ticketId, tenantId, authorId, authorRole, content, internal
     * @return the saved comment if the ticket was found; empty otherwise
     */
    public Optional<TicketComment> addComment(AddCommentCommand command) {
        Optional<Ticket> optionalTicket = findTicketPort.findByIdAndTenantId(
            command.getTicketId(), command.getTenantId());

        if (optionalTicket.isEmpty()) {
            return Optional.empty();
        }

        String content = normalizeContent(command.getContent());
        String authorRole = command.getAuthorRole() != null && !command.getAuthorRole().isBlank()
            ? command.getAuthorRole().trim()
            : DEFAULT_AUTHOR_ROLE;

        Ticket ticket = optionalTicket.get();
        TicketComment comment = TicketComment.builder()
            .ticket(ticket)
            .authorId(command.getAuthorId())
            .authorRole(authorRole)
            .content(content)
            .internal(command.isInternal())
            .build();

        TicketComment saved = saveCommentPort.save(comment);
        return Optional.of(saved);
    }

    private static String normalizeContent(String content) {
        if (content == null || content.isBlank()) {
            throw new IllegalArgumentException("Comment content cannot be null or blank");
        }
        return content.trim();
    }
}
