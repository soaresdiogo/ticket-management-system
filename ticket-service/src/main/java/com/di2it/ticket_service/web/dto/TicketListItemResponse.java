package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Ticket item in a list response")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketListItemResponse {

    @Schema(description = "Ticket ID")
    private UUID id;

    @Schema(description = "Tenant ID")
    private UUID tenantId;

    @Schema(description = "Client (creator) ID")
    private UUID clientId;

    @Schema(description = "Ticket title")
    private String title;

    @Schema(description = "Ticket description")
    private String description;

    @Schema(description = "Current status")
    private String status;

    @Schema(description = "Priority")
    private String priority;

    @Schema(description = "Category")
    private String category;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
