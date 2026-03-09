package com.di2it.api_gateway.infrastructure.filter;

import com.di2it.api_gateway.application.domain.PropagatedUserHeaders;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("JwtUserContextExtractorImpl")
class JwtUserContextExtractorImplTest {

    private JwtUserContextExtractorImpl extractor;

    @BeforeEach
    void setUp() {
        extractor = new JwtUserContextExtractorImpl();
    }

    private static Jwt jwt(String subject, String role, String tenantId) {
        var builder = Jwt.withTokenValue("token")
            .header("alg", "RS256")
            .issuedAt(Instant.now())
            .expiresAt(Instant.now().plusSeconds(3600));
        if (subject != null) {
            builder = builder.subject(subject);
        }
        if (role != null) {
            builder = builder.claim("role", role);
        }
        if (tenantId != null) {
            builder = builder.claim("tenantId", tenantId);
        }
        return builder.build();
    }

    @Nested
    @DisplayName("extract")
    class Extract {

        @Test
        @DisplayName("returns PropagatedUserHeaders when all claims present")
        void allClaimsPresent_returnsHeaders() {
            Jwt jwt = jwt("a1b2c3d4-e5f6-7890-abcd-ef1234567890", "CLIENT", "t9e8n7a6n5t4-i3d2-1111-2222-333344445555");

            Optional<PropagatedUserHeaders> result = extractor.extract(jwt);

            assertThat(result).isPresent();
            assertThat(result.get().userId()).isEqualTo("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
            assertThat(result.get().role()).isEqualTo("CLIENT");
            assertThat(result.get().tenantId()).isEqualTo("t9e8n7a6n5t4-i3d2-1111-2222-333344445555");
        }

        @Test
        @DisplayName("returns empty when subject is null")
        void nullSubject_returnsEmpty() {
            Jwt withNullSub = Jwt.withTokenValue("t").header("alg", "RS256").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1))
                .claim("role", "CLIENT").claim("tenantId", "tenant-id").build();

            Optional<PropagatedUserHeaders> result = extractor.extract(withNullSub);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when subject is blank")
        void blankSubject_returnsEmpty() {
            Jwt jwt = jwt("   ", "CLIENT", "tenant-id");

            Optional<PropagatedUserHeaders> result = extractor.extract(jwt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when role is null")
        void nullRole_returnsEmpty() {
            Jwt jwt = Jwt.withTokenValue("t").header("alg", "RS256").subject("user-id").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1))
                .claim("tenantId", "tenant-id").build();

            Optional<PropagatedUserHeaders> result = extractor.extract(jwt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when tenantId is null")
        void nullTenantId_returnsEmpty() {
            Jwt jwt = Jwt.withTokenValue("t").header("alg", "RS256").subject("user-id").issuedAt(Instant.now()).expiresAt(Instant.now().plusSeconds(1))
                .claim("role", "CLIENT").build();

            Optional<PropagatedUserHeaders> result = extractor.extract(jwt);

            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("returns empty when role is blank")
        void blankRole_returnsEmpty() {
            Jwt jwt = jwt("user-id", "", "tenant-id");

            Optional<PropagatedUserHeaders> result = extractor.extract(jwt);

            assertThat(result).isEmpty();
        }
    }
}
