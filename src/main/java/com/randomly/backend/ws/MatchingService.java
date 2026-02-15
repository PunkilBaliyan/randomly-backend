package com.randomly.backend.ws;

import com.randomly.backend.ws.dto.MatchResponse;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MatchingService {

    private final SimpMessagingTemplate messagingTemplate;

    // company → queue of userIds
    private final Map<String, Queue<String>> queues = new ConcurrentHashMap<>();

    public MatchingService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public synchronized void joinQueue(String userId, String company) {
        queues.putIfAbsent(company, new ArrayDeque<>());
        Queue<String> queue = queues.get(company);

        if (!queue.isEmpty()) {
            String otherUser = queue.poll();
            String sessionId = UUID.randomUUID().toString();

// The user who was waiting becomes caller
            messagingTemplate.convertAndSend(
                    "/topic/match/" + otherUser,
                    new MatchResponse(sessionId, true)
            );

            messagingTemplate.convertAndSend(
                    "/topic/match/" + userId,
                    new MatchResponse(sessionId, false)
            );
            System.out.println("Match created. Caller = " + otherUser);
        } else {
            queue.add(userId);
        }
    }

    public synchronized void cancelQueue(String userId) {
        queues.values().forEach(queue -> queue.remove(userId));
    }
}
