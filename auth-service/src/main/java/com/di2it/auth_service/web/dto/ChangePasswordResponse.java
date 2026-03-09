package com.di2it.auth_service.web.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Response after successful password change")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordResponse {

    @Schema(description = "Confirmation message")
    private String message;
}
