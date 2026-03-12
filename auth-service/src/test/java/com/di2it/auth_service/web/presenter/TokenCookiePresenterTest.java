package com.di2it.auth_service.web.presenter;

import com.di2it.auth_service.config.CookieProperties;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseCookie;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

class TokenCookiePresenterTest {

    private CookieProperties cookieProperties;

    @BeforeEach
    void setUp() {
        cookieProperties = new CookieProperties();
        cookieProperties.setRefreshTokenName("tms_refresh_token");
        cookieProperties.setPath("/");
        cookieProperties.setHttpOnly(true);
        cookieProperties.setSecure(true);
        cookieProperties.setSameSite("Lax");
    }

    @Nested
    @DisplayName("setRefreshTokenCookie")
    class SetRefreshTokenCookie {

        @Test
        @DisplayName("builds cookie with value, maxAge, HttpOnly and Secure")
        void buildsCookie() {
            ResponseCookie cookie = TokenCookiePresenter.setRefreshTokenCookie(
                "opaque.refresh.token",
                604800L,
                cookieProperties
            );

            assertThat(cookie.getName()).isEqualTo("tms_refresh_token");
            assertThat(cookie.getValue()).isEqualTo("opaque.refresh.token");
            assertThat(cookie.getMaxAge()).isEqualTo(Duration.ofSeconds(604800L));
            assertThat(cookie.isHttpOnly()).isTrue();
            assertThat(cookie.isSecure()).isTrue();
            assertThat(cookie.getSameSite()).isEqualTo("Lax");
            assertThat(cookie.getPath()).isEqualTo("/");
        }
    }

    @Nested
    @DisplayName("clearRefreshTokenCookie")
    class ClearRefreshTokenCookie {

        @Test
        @DisplayName("builds cookie with maxAge 0 to clear")
        void clearsCookie() {
            ResponseCookie cookie = TokenCookiePresenter.clearRefreshTokenCookie(cookieProperties);

            assertThat(cookie.getName()).isEqualTo("tms_refresh_token");
            assertThat(cookie.getValue()).isEmpty();
            assertThat(cookie.getMaxAge()).isEqualTo(Duration.ZERO);
            assertThat(cookie.isHttpOnly()).isTrue();
        }
    }
}
