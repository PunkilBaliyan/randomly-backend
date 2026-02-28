package com.randomly.backend.ws;

import com.randomly.backend.session.SessionRegistry;
import com.randomly.backend.ws.dto.MatchResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for managing matchmaking queues.
 * Pairs users from the same company for real-time communication.
 */
@Service
@RequiredArgsConstructor
public class MatchingService {

    private static final Logger log = LoggerFactory.getLogger(MatchingService.class);

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    // company → queue of userIds
    private final Map<String, Queue<String>> queues = new ConcurrentHashMap<>();
    
    // Track which queue a user is in (for cancellation)
    private final Map<String, String> userCompanies = new ConcurrentHashMap<>();

    public synchronized void joinQueue(String userId, String company) {
        log.info("[MATCH] User {} joining queue for {}", userId, company);
        
        if (userId == null || userId.isBlank()) {
            log.warn("[MATCH] Null or blank userId");
            return;
        }
        
        if (company == null || company.isBlank()) {
            log.warn("[MATCH] Null or blank company");
            return;
        }

        queues.putIfAbsent(company, new ArrayDeque<>());
        Queue<String> queue = queues.get(company);

        if (!queue.isEmpty()) {
            // Match found!
            String otherUser = queue.poll();
            userCompanies.remove(otherUser);
            userCompanies.remove(userId);
            
            String sessionId = UUID.randomUUID().toString();
            
            // Create session in registry
            sessionRegistry.create(otherUser, userId, company);

            // The user who was waiting becomes caller (they sent OFFER first)
            log.info("[MATCH] Match created: {} (caller) <-> {} (answerer), sessionId={}", 
                    otherUser, userId, sessionId);
            
            messagingTemplate.convertAndSend(
                    "/topic/match/" + otherUser,
                    new MatchResponse(sessionId, true)
            );

            messagingTemplate.convertAndSend(
                    "/topic/match/" + userId,
                    new MatchResponse(sessionId, false)
            );
        } else {
            // No match yet, add to queue
            log.info("[MATCH] No match yet, {} waiting in queue for {}", userId, company);
            queue.add(userId);
            userCompanies.put(userId, company);
        }
    }

    public synchronized void cancelQueue(String userId, String company) {
        log.info("[MATCH] User {} canceling queue for {}", userId, company);
        
        if (userId == null) return;
        
        Queue<String> queue = queues.get(company);
        if (queue != null) {
            queue.remove(userId);
        }
        userCompanies.remove(userId);
        
        log.info("[MATCH] User {} removed from queue", userId);
    }
    
    /**
     * Remove user from any queue they're in.
     * Called on WebSocket disconnect.
     */
    public synchronized void removeUserFromQueues(String userId) {
        log.info("[MATCH] Removing user {} from all queues", userId);
        
        String company = userCompanies.get(userId);
        if (company != null) {
            Queue<String> queue = queues.get(company);
            if (queue != null) {
                queue.remove(userId);
            }
            userCompanies.remove(userId);
        }
    }
}
