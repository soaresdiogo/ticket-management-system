package com.di2it.ticket_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Request body to add a comment to a ticket")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddCommentRequest {

    @Schema(
        description = "Comment text",
        example = "Please check the attached invoice.",
        requiredMode = Schema.RequiredMode.REQUIRED)
    @NotBlank(message = "Content is required")
    @Size(max = 10_000)
    private String content;

    @Schema(description = "When true, comment is visible only to office/accountant users", example = "false")
    @Builder.Default
    private boolean internal = false;
}
