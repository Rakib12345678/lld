package com.rockydrive.notification.websocket;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class WebSocketSessionRegistryTest {

    private WebSocketSessionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new WebSocketSessionRegistry();
    }

    @Test
    void hasNoActiveSessionForUnknownUser() {
        assertThat(registry.hasActiveSession("user-1")).isFalse();
        assertThat(registry.getSessions("user-1")).isEmpty();
        assertThat(registry.activeUserCount()).isZero();
    }

    @Test
    void registersASingleSession() {
        WebSocketSession session = mock(WebSocketSession.class);

        registry.register("user-1", session);

        assertThat(registry.hasActiveSession("user-1")).isTrue();
        assertThat(registry.getSessions("user-1")).containsExactly(session);
        assertThat(registry.activeUserCount()).isEqualTo(1);
    }

    @Test
    void supportsMultipleDevicesForTheSameUser() {
        WebSocketSession laptop = mock(WebSocketSession.class);
        WebSocketSession phone = mock(WebSocketSession.class);

        registry.register("user-1", laptop);
        registry.register("user-1", phone);

        Set<WebSocketSession> sessions = registry.getSessions("user-1");
        assertThat(sessions).containsExactlyInAnyOrder(laptop, phone);
        assertThat(registry.activeUserCount()).isEqualTo(1);
    }

    @Test
    void keepsSessionsOfDifferentUsersIsolated() {
        WebSocketSession sessionA = mock(WebSocketSession.class);
        WebSocketSession sessionB = mock(WebSocketSession.class);

        registry.register("user-1", sessionA);
        registry.register("user-2", sessionB);

        assertThat(registry.getSessions("user-1")).containsExactly(sessionA);
        assertThat(registry.getSessions("user-2")).containsExactly(sessionB);
        assertThat(registry.activeUserCount()).isEqualTo(2);
    }

    @Test
    void unregisteringOneDeviceKeepsTheOtherActive() {
        WebSocketSession laptop = mock(WebSocketSession.class);
        WebSocketSession phone = mock(WebSocketSession.class);
        registry.register("user-1", laptop);
        registry.register("user-1", phone);

        registry.unregister("user-1", laptop);

        assertThat(registry.hasActiveSession("user-1")).isTrue();
        assertThat(registry.getSessions("user-1")).containsExactly(phone);
    }

    @Test
    void unregisteringTheLastSessionRemovesTheUserEntirely() {
        WebSocketSession session = mock(WebSocketSession.class);
        registry.register("user-1", session);

        registry.unregister("user-1", session);

        assertThat(registry.hasActiveSession("user-1")).isFalse();
        assertThat(registry.getSessions("user-1")).isEmpty();
        // No leaked empty-set entry for the now-offline user.
        assertThat(registry.activeUserCount()).isZero();
    }

    @Test
    void unregisteringAnUnknownSessionIsANoOp() {
        WebSocketSession session = mock(WebSocketSession.class);

        registry.unregister("user-1", session);

        assertThat(registry.hasActiveSession("user-1")).isFalse();
        assertThat(registry.activeUserCount()).isZero();
    }
}
