package com.rockydrive.auth.service;

import com.rockydrive.auth.config.JwtProperties;
import com.rockydrive.auth.exception.InvalidTokenException;
import com.rockydrive.auth.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TokenServiceTest {

    private TokenService tokenService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        JwtProperties properties = new JwtProperties(
                "unit-test-secret-key-must-be-at-least-32-bytes-long!!",
                15,
                30
        );
        tokenService = new TokenService(properties);

        user = new User("user@example.com", "hashed-password", "Test User");
        setId(user, UUID.randomUUID());
    }

    @Test
    void generatedAccessTokenValidatesBackToTheSameUserId() {
        String accessToken = tokenService.generateAccessToken(user);

        UUID resolvedUserId = tokenService.validateAccessTokenAndGetUserId(accessToken);

        assertThat(resolvedUserId).isEqualTo(user.getId());
    }

    @Test
    void rejectsAnAccessTokenSignedWithADifferentSecret() throws Exception {
        JwtProperties otherProperties = new JwtProperties(
                "a-completely-different-unit-test-secret-key-32bytes",
                15,
                30
        );
        TokenService otherTokenService = new TokenService(otherProperties);
        String tokenFromOtherService = otherTokenService.generateAccessToken(user);

        assertThatThrownBy(() -> tokenService.validateAccessTokenAndGetUserId(tokenFromOtherService))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rawRefreshTokensAreUniqueAndTheirHashIsDeterministic() {
        String rawToken = tokenService.generateRawRefreshToken();
        String otherRawToken = tokenService.generateRawRefreshToken();

        assertThat(rawToken).isNotEqualTo(otherRawToken);
        assertThat(tokenService.hashToken(rawToken)).isEqualTo(tokenService.hashToken(rawToken));
        assertThat(tokenService.hashToken(rawToken)).isNotEqualTo(tokenService.hashToken(otherRawToken));
    }

    private static void setId(User user, UUID id) throws Exception {
        var field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
