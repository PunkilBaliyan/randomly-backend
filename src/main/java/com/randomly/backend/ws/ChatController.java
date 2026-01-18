package com.randomly.backend.ws;

import com.randomly.backend.session.ChatSession;
import com.randomly.backend.session.SessionRegistry;
import com.randomly.backend.ws.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.time.Instant;

@Controller
@RequiredArgsConstructor
public class ChatController {

    private static final Logger log = LoggerFactory.getLogger(ChatController.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    @MessageMapping("/chat/send")
    public void send(ChatMessage message) {
        log.debug("Received STOMP SEND frame: {}", message);

        ChatSession session = sessionRegistry
                .get(message.sessionId())
                .orElse(null);

        if (session == null) {
            log.warn("Chat session not found for sessionId={}", message.sessionId());
            return;
        }

        boolean authorized =
                message.fromUserId().equals(session.userA()) ||
                        message.fromUserId().equals(session.userB());

        if (!authorized) {
            log.warn("Unauthorized chat send attempt user={} for session={}", message.fromUserId(), message.sessionId());
            return;
        }

        ChatMessage enriched = new ChatMessage(
                message.sessionId(),
                message.fromUserId(),
                message.text(),
                Instant.now()
        );

        log.debug("Sending message to topic /topic/session/{} : {}", message.sessionId(), enriched);
        messagingTemplate.convertAndSend(
                "/topic/session/" + message.sessionId(),
                enriched
        );
    }
}