package com.rockydrive.notification.config;

import com.rockydrive.notification.websocket.JwtHandshakeInterceptor;
import com.rockydrive.notification.websocket.SyncWebSocketHandler;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/** Registers the /ws/sync endpoint, gated by {@link JwtHandshakeInterceptor}. */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final SyncWebSocketHandler syncWebSocketHandler;
    private final JwtHandshakeInterceptor jwtHandshakeInterceptor;

    public WebSocketConfig(SyncWebSocketHandler syncWebSocketHandler,
                            JwtHandshakeInterceptor jwtHandshakeInterceptor) {
        this.syncWebSocketHandler = syncWebSocketHandler;
        this.jwtHandshakeInterceptor = jwtHandshakeInterceptor;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(syncWebSocketHandler, "/ws/sync")
                .addInterceptors(jwtHandshakeInterceptor)
                // Clients (web/desktop/mobile) may be served from any rockydrive front-end origin;
                // auth is enforced by the JWT handshake interceptor, not by origin checking.
                .setAllowedOrigins("*");
    }
}
