package com.rockydrive.auth.service;

import com.rockydrive.auth.config.JwtProperties;
import com.rockydrive.auth.exception.InvalidTokenException;
import com.rockydrive.auth.model.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates access tokens (short-lived JWTs, signed with a secret every other service
 * shares so they can validate locally) and refresh tokens (long-lived opaque random strings -
 * only their SHA-256 hash is ever persisted, see {@link com.rockydrive.auth.model.RefreshToken}).
 */
@Service
public class TokenService {

    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    private final JwtProperties jwtProperties;
    private final SecretKey signingKey;

    public TokenService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiry = now.plus(Duration.ofMinutes(jwtProperties.accessTokenTtlMinutes()));
        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("email", user.getEmail())
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiry))
                .signWith(signingKey)
                .compact();
    }

    public long accessTokenTtlSeconds() {
        return Duration.ofMinutes(jwtProperties.accessTokenTtlMinutes()).toSeconds();
    }

    public Duration refreshTokenTtl() {
        return Duration.ofDays(jwtProperties.refreshTokenTtlDays());
    }

    /** Returns the user id encoded in the access token's subject claim, after verifying its signature and expiry. */
    public UUID validateAccessTokenAndGetUserId(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return UUID.fromString(claims.getSubject());
        } catch (ExpiredJwtException e) {
            throw new InvalidTokenException("Access token has expired");
        } catch (JwtException | IllegalArgumentException e) {
            throw new InvalidTokenException("Access token is invalid");
        }
    }

    /** A cryptographically random opaque string handed to the client; only its hash is stored. */
    public String generateRawRefreshToken() {
        byte[] bytes = new byte[64];
        SECURE_RANDOM.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public String hashToken(String rawToken) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
