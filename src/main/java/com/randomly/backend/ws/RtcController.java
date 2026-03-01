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
        System.out.println("\n========================================");
        System.out.println("[RTC-BACKEND] RTC SIGNAL RECEIVED");
        System.out.println("========================================");
        System.out.println("[RTC-BACKEND] From User: " + (signal != null ? signal.fromUserId() : "NULL"));
        System.out.println("[RTC-BACKEND] Session: " + (signal != null ? signal.sessionId() : "NULL"));
        System.out.println("[RTC-BACKEND] Type: " + (signal != null ? signal.type() : "NULL"));
        System.out.println("========================================\n");

        log.debug("[RTC] Received signal from {}: type={}, session={}", 
                signal.fromUserId(), signal.type(), signal.sessionId());

        if (signal == null || signal.sessionId() == null || 
                signal.fromUserId() == null || signal.type() == null) {
            log.warn("[RTC] Invalid signal: missing required fields");
            System.out.println("[RTC-BACKEND] ✗ INVALID SIGNAL - Missing required fields\n");
            return;
        }

        ChatSession session = sessionRegistry
                .get(signal.sessionId())
                .orElse(null);

        if (session == null) {
            log.warn("[RTC] Session not found for sessionId={}", signal.sessionId());
            System.out.println("[RTC-BACKEND] ✗ SESSION NOT FOUND\n");
            return;
        }

        System.out.println("[RTC-BACKEND] ✓ Session found: " + session.userA().substring(0, 8) + " <-> " + session.userB().substring(0, 8));
        
        boolean authorized =
                signal.fromUserId().equals(session.userA()) ||
                        signal.fromUserId().equals(session.userB());

        if (!authorized) {
            log.warn("[RTC] Unauthorized RTC signal from user={} for session={}", 
                    signal.fromUserId(), signal.sessionId());
            System.out.println("[RTC-BACKEND] ✗ UNAUTHORIZED\n");
            return;
        }

        System.out.println("[RTC-BACKEND] ✓ Authorized - Broadcasting " + signal.type());

        log.debug("[RTC] Broadcasting {} to /topic/session/{}/rtc", 
                signal.type(), signal.sessionId());

        messagingTemplate.convertAndSend(
                "/topic/session/" + signal.sessionId() + "/rtc",
                signal
        );
        
        System.out.println("[RTC-BACKEND] ✓ " + signal.type() + " BROADCASTED\n");
    }
}