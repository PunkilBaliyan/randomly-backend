package com.randomly.backend.ws;

import com.randomly.backend.session.ChatSession;
import com.randomly.backend.session.SessionRegistry;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
public class MatchmakingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final SessionRegistry sessionRegistry;

    // One FIFO queue per company
    private final Map<String, Queue<String>> queues = new ConcurrentHashMap<>();

    public MatchmakingController(
            SimpMessagingTemplate messagingTemplate,
            SessionRegistry sessionRegistry
    ) {
        this.messagingTemplate = messagingTemplate;
        this.sessionRegistry = sessionRegistry;
    }

    @MessageMapping("/join")
    public void joinQueue(JoinRequest req) {

        queues.putIfAbsent(req.company(), new ConcurrentLinkedQueue<>());
        Queue<String> queue = queues.get(req.company());

        if (!queue.isEmpty()) {
            String otherUser = queue.poll();

            // ✅ Create authoritative chat session
            ChatSession session = sessionRegistry.create(
                    otherUser,
                    req.userId(),
                    req.company()
            );

            // ✅ Notify both users with SAME sessionId
            messagingTemplate.convertAndSend(
                    "/topic/match/" + otherUser,
                    session.sessionId()
            );

            messagingTemplate.convertAndSend(
                    "/topic/match/" + req.userId(),
                    session.sessionId()
            );

        } else {
            // No one waiting → enqueue user
            queue.add(req.userId());
        }
    }

    @MessageMapping("/cancel")
    public void cancelQueue(JoinRequest req) {
        Queue<String> queue = queues.get(req.company());
        if (queue != null) {
            queue.remove(req.userId());
        }
    }
}
