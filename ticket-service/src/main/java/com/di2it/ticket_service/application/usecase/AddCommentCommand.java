package com.di2it.ticket_service.application.usecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to add a comment to a ticket. Contains context from the gateway (tenant, user, role).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentCommand {

    private UUID ticketId;
    private UUID tenantId;
    private UUID authorId;
    private String authorRole;
    private String content;
    private boolean internal;
}
