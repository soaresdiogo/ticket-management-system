package com.di2it.auth_service.web;

import com.di2it.auth_service.domain.entity.Tenant;
import com.di2it.auth_service.domain.entity.User;
import com.di2it.auth_service.domain.repository.UserRepository;
import com.di2it.auth_service.web.dto.InternalUserEmailResponse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InternalUserControllerTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private InternalUserController internalUserController;

    @Test
    @DisplayName("getUserEmail returns 200 with id and email when user exists")
    void getUserEmail_returnsOkWhenUserExists() {
        UUID userId = UUID.randomUUID();
        Tenant tenant = Tenant.builder().id(UUID.randomUUID()).name("T").email("t@t.com").build();
        User user = User.builder()
            .id(userId)
            .tenant(tenant)
            .email("client@example.com")
            .passwordHash("hash")
            .fullName("Client")
            .role("CLIENT")
            .build();
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        ResponseEntity<InternalUserEmailResponse> result =
            internalUserController.getUserEmail(userId);

        assertThat(result.getStatusCode().value()).isEqualTo(200);
        assertThat(result.getBody()).isNotNull();
        assertThat(result.getBody().id()).isEqualTo(userId);
        assertThat(result.getBody().email()).isEqualTo("client@example.com");
    }

    @Test
    @DisplayName("getUserEmail returns 404 when user not found")
    void getUserEmail_returnsNotFoundWhenUserMissing() {
        UUID userId = UUID.randomUUID();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        ResponseEntity<InternalUserEmailResponse> result =
            internalUserController.getUserEmail(userId);

        assertThat(result.getStatusCode().value()).isEqualTo(404);
        assertThat(result.getBody()).isNull();
    }
}
