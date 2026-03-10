package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request body to change a ticket's status")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangeTicketStatusRequest {

    @Schema(description = "New status (e.g. OPEN, IN_PROGRESS, RESOLVED, CLOSED)", example = "IN_PROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Status is required")
    @Size(max = 50)
    private String status;
}
