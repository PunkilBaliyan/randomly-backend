package com.randomly.backend.ws;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

@Controller
public class MatchmakingController {

    private final SimpMessagingTemplate messagingTemplate;
    private final Map<String, Queue<String>> queues = new ConcurrentHashMap<>();

    public MatchmakingController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/join")
    public void joinQueue(JoinRequest req) {
        queues.putIfAbsent(req.company(), new ConcurrentLinkedQueue<>());
        Queue<String> queue = queues.get(req.company());

        if (!queue.isEmpty()) {
            String otherUser = queue.poll();
            String sessionId = UUID.randomUUID().toString();

            messagingTemplate.convertAndSend(
                    "/topic/match/" + otherUser,
                    sessionId
            );
            messagingTemplate.convertAndSend(
                    "/topic/match/" + req.userId(),
                    sessionId
            );
        } else {
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
