package com.rockydrive.notification.websocket;

import com.rockydrive.notification.security.JwtValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Authenticates the /ws/sync handshake by validating a JWT locally (no call to auth-service).
 *
 * <p>Browsers cannot set custom headers on a WebSocket upgrade request, so the token is accepted
 * as a {@code token} query parameter (e.g. {@code /ws/sync?token=...}); an {@code Authorization:
 * Bearer ...} header is also accepted as a fallback for non-browser clients. Handshakes with a
 * missing or invalid token are rejected with 401 before the socket is ever opened.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private static final Logger log = LoggerFactory.getLogger(JwtHandshakeInterceptor.class);
    private static final String TOKEN_QUERY_PARAM = "token";
    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtValidator jwtValidator;

    public JwtHandshakeInterceptor(JwtValidator jwtValidator) {
        this.jwtValidator = jwtValidator;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        String token = extractToken(request);
        Optional<String> userId = jwtValidator.validateAndGetUserId(token);
        if (userId.isEmpty()) {
            log.debug("Rejecting /ws/sync handshake: missing or invalid JWT");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return false;
        }
        attributes.put(SyncWebSocketHandler.USER_ID_ATTR, userId.get());
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }

    private String extractToken(ServerHttpRequest request) {
        List<String> fromQuery = UriComponentsBuilder.fromUri(request.getURI())
                .build()
                .getQueryParams()
                .get(TOKEN_QUERY_PARAM);
        if (fromQuery != null && !fromQuery.isEmpty() && !fromQuery.get(0).isBlank()) {
            return fromQuery.get(0);
        }

        List<String> authHeaders = request.getHeaders().get(AUTHORIZATION_HEADER);
        if (authHeaders != null) {
            for (String header : authHeaders) {
                if (header != null && header.startsWith(BEARER_PREFIX)) {
                    return header.substring(BEARER_PREFIX.length());
                }
            }
        }
        return null;
    }
}
