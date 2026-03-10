package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request body to create a new ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {

    @Schema(description = "Ticket title", example = "Invoice discrepancy", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Title is required")
    @Size(max = 255)
    private String title;

    @Schema(description = "Detailed description of the issue", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Description is required")
    private String description;

    @Schema(description = "Priority: LOW, NORMAL, HIGH, URGENT", example = "NORMAL")
    @Size(max = 50)
    private String priority;

    @Schema(description = "Optional category for the ticket", example = "Billing")
    @Size(max = 100)
    private String category;
}
