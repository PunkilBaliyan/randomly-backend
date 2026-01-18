package com.randomly.backend.ws;

import com.randomly.backend.session.ChatSession;
import com.randomly.backend.session.SessionRegistry;
import com.randomly.backend.ws.dto.ChatMessage;
import com.randomly.backend.ws.dto.TypingEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    @MessageMapping("/chat/send")
    public void send(ChatMessage message) {

        // 🔐 Validate session
        ChatSession session = sessionRegistry
                .get(message.sessionId())
                .orElse(null);

        if (session == null) {
            return; // invalid / expired session
        }

        // 🔐 Validate sender is part of session
        boolean authorized =
                message.fromUserId().equals(session.userA()) ||
                        message.fromUserId().equals(session.userB());

        if (!authorized) {
            return;
        }

        // ✅ Broadcast message to session topic
        ChatMessage enriched = new ChatMessage(
                message.sessionId(),
                message.fromUserId(),
                message.text(),
                Instant.now()
        );

        messagingTemplate.convertAndSend(
                "/topic/session/" + message.sessionId(),
                enriched
        );
    }
    @MessageMapping("/chat/typing")
    public void typing(TypingEvent event) {

        ChatSession session = sessionRegistry
                .get(event.sessionId())
                .orElse(null);

        if (session == null) return;

        boolean authorized =
                event.fromUserId().equals(session.userA()) ||
                        event.fromUserId().equals(session.userB());

        if (!authorized) return;

        messagingTemplate.convertAndSend(
                "/topic/session/" + event.sessionId() + "/typing",
                event
        );
    }

}
