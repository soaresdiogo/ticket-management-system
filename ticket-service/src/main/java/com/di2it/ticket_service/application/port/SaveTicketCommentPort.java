package com.di2it.ticket_service.application.port;

import com.di2it.ticket_service.domain.entity.TicketComment;

/**
 * Port for persisting a ticket comment.
 */
public interface SaveTicketCommentPort {

    /**
     * Saves the given comment.
     *
     * @param comment the comment to save (must have ticket, authorId, authorRole, content, internal set)
     * @return the persisted comment (with id and createdAt populated)
     */
    TicketComment save(TicketComment comment);
}
