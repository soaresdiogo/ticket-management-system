package com.di2it.auth_service.web.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChangePasswordResponse {

    /**
     * Message confirming the password was changed.
     */
    private String message;
}
