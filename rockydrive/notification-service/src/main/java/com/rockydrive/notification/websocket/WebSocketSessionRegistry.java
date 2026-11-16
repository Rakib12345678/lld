package com.rockydrive.notification.websocket;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory registry mapping userId -&gt; the set of currently-open WebSocket sessions for that
 * user (one per connected device/tab). Backed by {@link ConcurrentHashMap} so concurrent
 * handshakes/disconnects from multiple Kafka listener / WebSocket threads are safe.
 *
 * <p>Entries are removed as soon as a user's last session closes, so this map never accumulates
 * empty sets for users who have gone offline.
 */
@Component
public class WebSocketSessionRegistry {

    private final Map<String, Set<WebSocketSession>> sessionsByUser = new ConcurrentHashMap<>();

    /** Registers a newly-established session for the given user (additive - supports multiple devices). */
    public void register(String userId, WebSocketSession session) {
        sessionsByUser
                .computeIfAbsent(userId, ignored -> ConcurrentHashMap.newKeySet())
                .add(session);
    }

    /**
     * Removes a session for the given user, e.g. on {@code afterConnectionClosed}. If this was the
     * user's last open session, the user's entry is removed entirely to avoid leaking empty sets.
     */
    public void unregister(String userId, WebSocketSession session) {
        sessionsByUser.computeIfPresent(userId, (id, sessions) -> {
            sessions.remove(session);
            return sessions.isEmpty() ? null : sessions;
        });
    }

    /** Returns an immutable snapshot of the user's currently open sessions (empty if none/offline). */
    public Set<WebSocketSession> getSessions(String userId) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        return sessions == null ? Collections.emptySet() : Set.copyOf(sessions);
    }

    /** Whether the user currently has at least one open WebSocket session on this instance. */
    public boolean hasActiveSession(String userId) {
        Set<WebSocketSession> sessions = sessionsByUser.get(userId);
        return sessions != null && !sessions.isEmpty();
    }

    /** Number of distinct users with at least one open session. Exposed for tests/observability. */
    public int activeUserCount() {
        return sessionsByUser.size();
    }
}
