package com.di2it.ticket_service.application.usecase;

import lombok.Builder;
import lombok.Value;

import java.util.UUID;

/**
 * Command to create a new ticket. Holds tenant and client from context (e.g. JWT/headers).
 */
@Value
@Builder
public class CreateTicketCommand {

    UUID tenantId;
    UUID clientId;
    String title;
    String description;
    String priority;
    String category;
}
