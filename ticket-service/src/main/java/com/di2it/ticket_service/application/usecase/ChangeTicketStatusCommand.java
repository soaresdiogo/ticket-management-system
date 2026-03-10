package com.di2it.ticket_service.application.usecase;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Command to change a ticket's status. Contains context from the gateway (tenant, user).
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTicketStatusCommand {

    private UUID ticketId;
    private UUID tenantId;
    private UUID userId;
    private String newStatus;
}
