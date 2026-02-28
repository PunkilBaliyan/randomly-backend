package com.randomly.backend.session;

import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registry for managing active chat sessions.
 * Tracks which users are in which sessions.
 */
@Component
public class SessionRegistry {

    private static final Logger log = LoggerFactory.getLogger(SessionRegistry.class);

    private final Map<String, ChatSession> sessions = new ConcurrentHashMap<>();
    
    // Reverse mapping: userId -> sessionId (for quick cleanup)
    private final Map<String, String> userSessions = new ConcurrentHashMap<>();

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
        userSessions.put(userA, sessionId);
        userSessions.put(userB, sessionId);

        log.info("[SESSION] Created session: {} for users {} and {} in company {}", 
                sessionId, userA, userB, company);

        return session;
    }

    public Optional<ChatSession> get(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }

    public void remove(String sessionId) {
        ChatSession session = sessions.remove(sessionId);
        if (session != null) {
            userSessions.remove(session.userA());
            userSessions.remove(session.userB());
            log.info("[SESSION] Removed session: {}", sessionId);
        }
    }

    /**
     * Get the session a user is in, if any
     */
    public Optional<ChatSession> getByUser(String userId) {
        String sessionId = userSessions.get(userId);
        if (sessionId != null) {
            return get(sessionId);
        }
        return Optional.empty();
    }

    /**
     * Remove user from their session
     */
    public void removeUser(String userId) {
        String sessionId = userSessions.remove(userId);
        if (sessionId != null) {
            log.info("[SESSION] Removing user {} from session {}", userId, sessionId);
            
            ChatSession session = sessions.get(sessionId);
            if (session != null) {
                // Remove the other user too
                if (userId.equals(session.userA())) {
                    userSessions.remove(session.userB());
                } else {
                    userSessions.remove(session.userA());
                }
            }
            sessions.remove(sessionId);
        }
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }
}
