package com.randomly.backend.ws;

import com.randomly.backend.session.ChatSession;
import com.randomly.backend.session.SessionRegistry;
import com.randomly.backend.ws.dto.RtcSignal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class RtcController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    @MessageMapping("/rtc/signal")
    public void signal(RtcSignal signal) {

        // 1️⃣ Validate session exists
        ChatSession session = sessionRegistry
                .get(signal.sessionId())
                .orElse(null);

        if (session == null) {
            return;
        }

        // 2️⃣ Validate sender is part of session
        boolean authorized =
                signal.fromUserId().equals(session.userA()) ||
                        signal.fromUserId().equals(session.userB());

        if (!authorized) {
            return;
        }

        // 3️⃣ Relay signal to session RTC topic
        messagingTemplate.convertAndSend(
                "/topic/session/" + signal.sessionId() + "/rtc",
                signal
        );
    }
}
