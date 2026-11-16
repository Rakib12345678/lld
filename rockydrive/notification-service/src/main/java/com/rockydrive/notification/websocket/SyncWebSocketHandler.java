package com.rockydrive.notification.websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * Handles the /ws/sync WebSocket lifecycle. The userId is resolved during the handshake (see
 * {@link JwtHandshakeInterceptor}) and stashed in the session attributes; this handler's only
 * job is to keep {@link WebSocketSessionRegistry} in sync with which sessions are actually open.
 *
 * <p>Clients don't send commands over this channel - it is a one-way "wake up and sync" signal.
 * The client is expected to call sync-service's delta endpoint to fetch the actual changes.
 */
@Component
public class SyncWebSocketHandler extends TextWebSocketHandler {

    /** Session attribute key the handshake interceptor stores the authenticated userId under. */
    public static final String USER_ID_ATTR = "userId";

    private static final Logger log = LoggerFactory.getLogger(SyncWebSocketHandler.class);

    private final WebSocketSessionRegistry sessionRegistry;

    public SyncWebSocketHandler(WebSocketSessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        String userId = resolveUserId(session);
        if (userId == null) {
            // Should not happen if the handshake interceptor rejected unauthenticated requests,
            // but guard against it defensively rather than registering an anonymous session.
            session.close(CloseStatus.POLICY_VIOLATION);
            return;
        }
        sessionRegistry.register(userId, session);
        log.debug("WebSocket session {} registered for user {}", session.getId(), userId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String userId = resolveUserId(session);
        if (userId != null) {
            sessionRegistry.unregister(userId, session);
            log.debug("WebSocket session {} unregistered for user {} ({})", session.getId(), userId, status);
        }
    }

    private String resolveUserId(WebSocketSession session) {
        Object userId = session.getAttributes().get(USER_ID_ATTR);
        return userId instanceof String s ? s : null;
    }
}
