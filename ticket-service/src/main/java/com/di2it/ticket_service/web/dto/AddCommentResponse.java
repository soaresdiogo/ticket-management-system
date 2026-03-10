package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Schema(description = "Response after adding a comment to a ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentResponse {

    @Schema(description = "Comment ID")
    private UUID id;

    @Schema(description = "Comment text")
    private String content;

    @Schema(description = "Author user ID")
    private UUID authorId;

    @Schema(description = "Author role (e.g. CLIENT, ACCOUNTANT)")
    private String authorRole;

    @Schema(description = "Whether the comment is internal (office-only)")
    private boolean internal;

    @Schema(description = "Creation timestamp")
    private LocalDateTime createdAt;
}
