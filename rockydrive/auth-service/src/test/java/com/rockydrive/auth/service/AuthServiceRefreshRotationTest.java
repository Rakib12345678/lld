package com.rockydrive.auth.service;

import com.rockydrive.auth.config.JwtProperties;
import com.rockydrive.auth.dto.AuthResponse;
import com.rockydrive.auth.dto.RefreshRequest;
import com.rockydrive.auth.exception.InvalidTokenException;
import com.rockydrive.auth.model.RefreshToken;
import com.rockydrive.auth.model.User;
import com.rockydrive.auth.repository.DeviceRepository;
import com.rockydrive.auth.repository.RefreshTokenRepository;
import com.rockydrive.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Refresh rotation is the security-critical path: a presented refresh token must be revoked
 * the moment it's used, and reuse of an already-revoked (or expired/unknown) token must be
 * rejected rather than silently issuing a new pair.
 */
class AuthServiceRefreshRotationTest {

    private UserRepository userRepository;
    private RefreshTokenRepository refreshTokenRepository;
    private AuthService authService;
    private User user;

    @BeforeEach
    void setUp() throws Exception {
        userRepository = mock(UserRepository.class);
        refreshTokenRepository = mock(RefreshTokenRepository.class);
        DeviceRepository deviceRepository = mock(DeviceRepository.class);

        JwtProperties jwtProperties = new JwtProperties(
                "unit-test-secret-key-must-be-at-least-32-bytes-long!!", 15, 30
        );
        TokenService tokenService = new TokenService(jwtProperties);

        authService = new AuthService(
                userRepository, refreshTokenRepository, deviceRepository,
                new BCryptPasswordEncoder(), tokenService
        );

        user = new User("user@example.com", "hashed-password", "Test User");
        setId(user, UUID.randomUUID());

        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void rotatingAUsableRefreshTokenRevokesItAndReturnsANewPair() {
        String rawRefreshToken = "raw-refresh-token-value";
        RefreshToken storedToken = new RefreshToken(
                user, sha256(rawRefreshToken), "device-1", Instant.now(), Instant.now().plusSeconds(3600)
        );
        when(refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken))).thenReturn(Optional.of(storedToken));

        AuthResponse response = authService.refresh(new RefreshRequest(rawRefreshToken, null, null));

        assertThat(response.refreshToken()).isNotEqualTo(rawRefreshToken);
        assertThat(storedToken.isRevoked()).isTrue();
        assertThat(storedToken.getReplacedByTokenHash()).isEqualTo(sha256(response.refreshToken()));
    }

    @Test
    void rotatingAnAlreadyRevokedRefreshTokenIsRejected() {
        String rawRefreshToken = "raw-refresh-token-value";
        RefreshToken storedToken = new RefreshToken(
                user, sha256(rawRefreshToken), "device-1", Instant.now(), Instant.now().plusSeconds(3600)
        );
        storedToken.revoke("some-other-hash");
        when(refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken))).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(rawRefreshToken, null, null)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rotatingAnUnknownRefreshTokenIsRejected() {
        when(refreshTokenRepository.findByTokenHash(any())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest("not-a-real-token", null, null)))
                .isInstanceOf(InvalidTokenException.class);
    }

    @Test
    void rotatingAnExpiredRefreshTokenIsRejected() {
        String rawRefreshToken = "raw-refresh-token-value";
        RefreshToken storedToken = new RefreshToken(
                user, sha256(rawRefreshToken), "device-1", Instant.now().minusSeconds(7200), Instant.now().minusSeconds(3600)
        );
        when(refreshTokenRepository.findByTokenHash(sha256(rawRefreshToken))).thenReturn(Optional.of(storedToken));

        assertThatThrownBy(() -> authService.refresh(new RefreshRequest(rawRefreshToken, null, null)))
                .isInstanceOf(InvalidTokenException.class);
    }

    private String sha256(String rawToken) {
        JwtProperties jwtProperties = new JwtProperties(
                "unit-test-secret-key-must-be-at-least-32-bytes-long!!", 15, 30
        );
        return new TokenService(jwtProperties).hashToken(rawToken);
    }

    private static void setId(User user, UUID id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}
