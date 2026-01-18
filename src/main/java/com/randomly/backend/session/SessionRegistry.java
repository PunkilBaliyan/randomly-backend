package com.randomly.backend.session;

import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SessionRegistry {

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();

    public ChatSession create(String userA, String userB, String company) {
        String sessionId = UUID.randomUUID().toString();

        ChatSession session = new ChatSession(
                sessionId,
                userA,
                userB,
                company,
                Instant.now()
        );

        sessions.put(sessionId, session);

        return session;
    }

    public Optional<ChatSession> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void remove(String sessionId) {
        sessions.remove(sessionId);
    }
}
