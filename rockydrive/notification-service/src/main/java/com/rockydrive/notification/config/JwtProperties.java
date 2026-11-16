package com.rockydrive.notification.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Shared-secret HMAC config used to validate JWTs presented on the /ws/sync handshake locally
 * (no synchronous call to auth-service). All rockydrive services that need to validate tokens are
 * expected to be configured with the same signing secret.
 *
 * <p>Uses the standardized {@code jwt.*} prefix so every service reads from the same
 * {@code JWT_SECRET} environment variable.
 */
@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(String secret) {
}
