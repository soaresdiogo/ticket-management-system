package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response after changing a ticket's status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTicketStatusResponse {

    @Schema(description = "Ticket ID")
    private UUID id;

    @Schema(description = "Current status after the change")
    private String status;

    @Schema(description = "Last update timestamp")
    private LocalDateTime updatedAt;
}
