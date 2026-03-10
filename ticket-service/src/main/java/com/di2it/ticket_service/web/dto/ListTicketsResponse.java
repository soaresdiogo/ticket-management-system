package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Schema(description = "Paginated list of tickets for the current user")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ListTicketsResponse {

    @Schema(description = "Tickets in this page")
    private List<TicketListItemResponse> content;

    @Schema(description = "Total number of tickets")
    private long totalElements;

    @Schema(description = "Total number of pages")
    private int totalPages;

    @Schema(description = "Current page index (0-based)")
    private int number;

    @Schema(description = "Page size")
    private int size;
}
