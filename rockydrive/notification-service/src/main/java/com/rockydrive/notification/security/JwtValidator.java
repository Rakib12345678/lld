package com.rockydrive.notification.security;

import com.rockydrive.notification.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Validates JWTs presented by clients on the /ws/sync WebSocket handshake, locally, using a
 * shared HMAC secret read from config. This mirrors the approach other rockydrive services use
 * for stateless auth and deliberately avoids a synchronous call to auth-service on every
 * connection attempt.
 */
@Component
public class JwtValidator {

    private static final Logger log = LoggerFactory.getLogger(JwtValidator.class);

    private final SecretKey signingKey;

    public JwtValidator(JwtProperties jwtProperties) {
        String secret = jwtProperties.secret();
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("jwt.secret must be configured");
        }
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Validates the token's signature and expiry, and extracts the user id.
     *
     * @param token raw JWT (no "Bearer " prefix)
     * @return the subject (userId) claim if the token is valid; empty otherwise
     */
    public Optional<String> validateAndGetUserId(String token) {
        if (token == null || token.isBlank()) {
            return Optional.empty();
        }
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            String userId = claims.getSubject();
            if (userId == null || userId.isBlank()) {
                return Optional.empty();
            }
            return Optional.of(userId);
        } catch (JwtException | IllegalArgumentException e) {
            log.debug("Rejected invalid JWT on /ws/sync handshake: {}", e.getMessage());
            return Optional.empty();
        }
    }
}
