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

        System.out.println("==================================");
        System.out.println("RTC SIGNAL RECEIVED:");
        System.out.println("Type: " + signal.type());
        System.out.println("From: " + signal.fromUserId());
        System.out.println("Session: " + signal.sessionId());

        ChatSession session = sessionRegistry
                .get(signal.sessionId())
                .orElse(null);

        if (session == null) {
            System.out.println("❌ Session not found");
            return;
        }

        boolean authorized =
                signal.fromUserId().equals(session.userA()) ||
                        signal.fromUserId().equals(session.userB());

        if (!authorized) {
            System.out.println("❌ Unauthorized sender");
            return;
        }

        System.out.println("✅ Broadcasting to /topic/session/" +
                signal.sessionId() + "/rtc");

        messagingTemplate.convertAndSend(
                "/topic/session/" + signal.sessionId() + "/rtc",
                signal
        );
    }
}