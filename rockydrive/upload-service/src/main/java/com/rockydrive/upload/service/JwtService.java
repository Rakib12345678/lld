package com.rockydrive.upload.service;

import com.rockydrive.upload.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

/**
 * Validates access tokens locally against the secret shared with auth-service. Never calls
 * auth-service synchronously to verify a token — that would put auth-service in the hot path of
 * every request across every service.
 */
@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(JwtProperties jwtProperties) {
        this.signingKey = Keys.hmacShaKeyFor(jwtProperties.secret().getBytes(StandardCharsets.UTF_8));
    }

    /** @return the {@code userId} (subject) claim, or throws if the token is missing/expired/invalid. */
    public String extractUserId(String bearerToken) {
        String token = stripBearerPrefix(bearerToken);
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
        return claims.getSubject();
    }

    private String stripBearerPrefix(String header) {
        if (header != null && header.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return header.substring(7);
        }
        return header;
    }
}
