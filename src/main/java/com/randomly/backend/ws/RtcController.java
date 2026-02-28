package com.randomly.backend.ws;

import com.randomly.backend.session.ChatSession;
import com.randomly.backend.session.SessionRegistry;
import com.randomly.backend.ws.dto.RtcSignal;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

/**
 * Handles WebRTC signaling (OFFER, ANSWER, ICE candidates, HANGUP).
 * Forwards signaling messages between peers for peer-to-peer video/audio connection.
 */
@Controller
@RequiredArgsConstructor
public class RtcController {

    private static final Logger log = LoggerFactory.getLogger(RtcController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    @MessageMapping("/rtc/signal")
    public void signal(RtcSignal signal) {
        log.debug("[RTC] Received signal from {}: type={}, session={}", 
                signal.fromUserId(), signal.type(), signal.sessionId());

        if (signal == null || signal.sessionId() == null || 
                signal.fromUserId() == null || signal.type() == null) {
            log.warn("[RTC] Invalid signal: missing required fields");
            return;
        }

        ChatSession session = sessionRegistry
                .get(signal.sessionId())
                .orElse(null);

        if (session == null) {
            log.warn("[RTC] Session not found for sessionId={}", signal.sessionId());
            return;
        }

        boolean authorized =
                signal.fromUserId().equals(session.userA()) ||
                        signal.fromUserId().equals(session.userB());

        if (!authorized) {
            log.warn("[RTC] Unauthorized RTC signal from user={} for session={}", 
                    signal.fromUserId(), signal.sessionId());
            return;
        }

        log.debug("[RTC] Broadcasting {} to /topic/session/{}/rtc", 
                signal.type(), signal.sessionId());

        messagingTemplate.convertAndSend(
                "/topic/session/" + signal.sessionId() + "/rtc",
                signal
        );
    }
}