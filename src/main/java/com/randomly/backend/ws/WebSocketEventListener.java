package com.randomly.backend.ws;

import com.randomly.backend.session.SessionRegistry;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles WebSocket lifecycle events.
 * Cleans up sessions and queues when users disconnect.
 */
@Component
@RequiredArgsConstructor
public class WebSocketEventListener {

    private static final Logger log = LoggerFactory.getLogger(WebSocketEventListener.class);

    private final SessionRegistry sessionRegistry;
    private final MatchingService matchingService;

    @EventListener
    public void handleWebSocketConnected(SessionConnectedEvent event) {
        log.info("[EVENT] User connected");
    }

    @EventListener
    public void handleWebSocketDisconnected(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = accessor.getSessionId();

        log.info("[EVENT] User disconnected: sessionId={}", sessionId);

        // TODO: If we track userId in session attributes, we can clean up here
        // For now, rely on frontend to call cancelQueue and hangup
    }
}
